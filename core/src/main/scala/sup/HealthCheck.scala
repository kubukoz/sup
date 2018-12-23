package sup

import cats.data.{EitherK, Tuple2K}
import cats.effect.Concurrent
import cats.{~>, Applicative, ApplicativeError, Apply, Eq, Functor, Monoid}
import cats.implicits._
import cats.effect.implicits._
import cats.tagless.FunctorK
import sup.transformed.{LeftMappedHealthCheck, MappedKHealthCheck, MappedResultHealthCheck, TransformedHealthCheck}
import cats.tagless.implicits._
import cats.temp.par._

/**
  * A health check.
  * F is the effect of making a healthcheck (e.g. IO for calls to external systems).
  *
  * H is the container of results. See [[HealthResult]] for examples.
  * */
abstract class HealthCheck[F[_], H[_]] {
  def check: F[HealthResult[H]]

  def leftMapK[G[_]](fg: F ~> G): HealthCheck[G, H] =
    new LeftMappedHealthCheck[F, G, H](this, fg)

  def transform[G[_], I[_]](f: F[HealthResult[H]] => G[HealthResult[I]]): HealthCheck[G, I] =
    new TransformedHealthCheck[F, G, H, I](this, f)

  def mapK[I[_]](f: H ~> I)(implicit F: Functor[F]): HealthCheck[F, I] = new MappedKHealthCheck(this, f)

  def mapResult[I[_]](f: HealthResult[H] => HealthResult[I])(implicit F: Functor[F]): HealthCheck[F, I] =
    new MappedResultHealthCheck(this, f)
}

object HealthCheck {

  /**
    * A healthcheck that always returns the supplied health value.
    * */
  def const[F[_]: Applicative, H[_]: Applicative](health: Health): HealthCheck[F, H] = new HealthCheck[F, H] {
    override val check: F[HealthResult[H]] = HealthResult.const[H](health).pure[F]
  }

  def liftF[F[_], H[_]](_check: F[HealthResult[H]]): HealthCheck[F, H] = new HealthCheck[F, H] {
    override val check: F[HealthResult[H]] = _check
  }

  /**
    * Combines two healthchecks by running the first one and recovering with the second one in case of failure in F.
    * */
  def either[F[_]: ApplicativeError[?[_], E], E, H[_], I[_]](a: HealthCheck[F, H],
                                                             b: HealthCheck[F, I]): HealthCheck[F, EitherK[H, I, ?]] =
    liftF {
      a.check
        .map(ar => EitherK.left[I](ar.value))
        .orElse(b.check.map(br => EitherK.right[H](br.value)))
        .map(HealthResult(_))
    }

  /**
    * Combines two healthchecks by running them both, then wrapping the result in a Tuple2K.
    * */
  def tupled[F[_]: Apply, H[_], I[_]](a: HealthCheck[F, H], b: HealthCheck[F, I]): HealthCheck[F, Tuple2K[H, I, ?]] =
    liftF {
      (a.check, b.check).mapN { (ac, bc) =>
        HealthResult(Tuple2K(ac.value, bc.value))
      }
    }

  /**
    * Combines two healthchecks by running them in parallel, then wrapping the result in a Tuple2K.
    * */
  def parTupled[F[_]: Par, H[_], I[_]](a: HealthCheck[F, H], b: HealthCheck[F, I]): HealthCheck[F, Tuple2K[H, I, ?]] =
    liftF {
      (a.check, b.check).parMapN { (ac, bc) =>
        HealthResult(Tuple2K(ac.value, bc.value))
      }
    }

  /**
    * Races two healthchecks against each other.
    * The first one to complete with a result (regardless of whether healthy or sick) will cancel the other.
    * */
  def race[F[_]: Concurrent, H[_], I[_]](a: HealthCheck[F, H], b: HealthCheck[F, I]): HealthCheck[F, EitherK[H, I, ?]] =
    liftF {
      a.check.race(b.check).map { e =>
        HealthResult(EitherK(e.bimap(_.value, _.value)))
      }
    }

  implicit def functorK[F[_]: Functor]: FunctorK[HealthCheck[F, ?[_]]] = new FunctorK[HealthCheck[F, ?[_]]] {
    override def mapK[G[_], H[_]](fgh: HealthCheck[F, G])(gh: G ~> H): HealthCheck[F, H] = fgh.mapK(gh)
  }

  implicit def checkMonoid[F[_]: Applicative, H[_]: Applicative](
    implicit M: Monoid[Health]): Monoid[HealthCheck[F, H]] =
    new Monoid[HealthCheck[F, H]] {
      override val empty: HealthCheck[F, H] = HealthCheck.const[F, H](M.empty)
      override def combine(x: HealthCheck[F, H], y: HealthCheck[F, H]): HealthCheck[F, H] = new HealthCheck[F, H] {
        override val check: F[HealthResult[H]] = Applicative.monoid[F, HealthResult[H]].combine(x.check, y.check)
      }
    }

  implicit def healthCheckEq[F[_], H[_]](implicit F: Eq[F[HealthResult[H]]]): Eq[HealthCheck[F, H]] = Eq.by(_.check)
}
