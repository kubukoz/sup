---
layout: docs
title: akka-http
---

sup has modules for <a href="https://doc.akka.io/docs/akka-http/current/index.html" target="_blank">akka-http</a> and <a href="https://circe.github.io/circe/" target="_blank">circe</a> (although you can use any JSON library compatible with akka-http):

```scala mdoc:passthrough
sup.microsite.sbtDependencies("akka-http", "circe")
```

Imports:
```scala mdoc:silent
import sup._
import sup.modules.akkahttp._, sup.modules.circe._
```

## What's included

### `healthCheckRoutes`, `healthCheckResponse`

It is possible to create a `Route` from a health check.

To do so, you'll need:

- a `Reducible` instance for your container of results
- a way to serialize the results (as defined per the implicit `ToEntityMarshaller[HealthResult[H]]`).
- a `cats.effect.std.Dispatcher[F]` instance for your health check's `F[_]`, or `cats.effect.Async[F]` (see examples below)

Once you have all three, use `healthCheckRoutes`:

```scala mdoc
import cats.effect.IO, cats.effect.Resource, cats.Id
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

// just some boilerplate to manage akka server
def serve(routes: Route): Resource[IO, ServerBinding] =
  Resource.make(IO(ActorSystem()))(as => IO.fromFuture(IO(as.terminate())).void).flatMap { implicit system =>
    Resource.make(IO.fromFuture(IO(Http().newServerAt("localhost", 8080).bind(routes))))(binding => IO.fromFuture(IO(binding.unbind())).void)
  }

val check: HealthCheck[IO, Id] = HealthCheck.const(Health.healthy)

// You can return this from your IOApp's `run`
val app = AkkaSup.instance[IO].flatMap { akkaSup =>

  val routes: Route = akkaSup.healthCheckRoutes[Id](check)

  serve(routes)
}.useForever
```

You can customize the path at which the health checks will be available (the default is `path("health-check")`).

Alternatively, you can get a raw response built from a check, inside the effect, using `healthCheckResponse` directly
and constructing your Route manually from that.


### `healthCheckRoutesWithContext`

If your health check's effect type requires information provided by the request (e.g. a tracing token header),
you can use `healthCheckRoutesWithContext` and provide a way to run the effect from a request.

For example:

```scala mdoc
import cats.data.ReaderT, cats.~>
import scala.concurrent.Future
//for IORuntime
import cats.effect.unsafe.implicits._

case class User()
type Token = String

type Eff[A] = ReaderT[IO, Token, A]

def findUserByToken(token: Token): IO[Option[User]] = ???

val checkReader: HealthCheck[Eff, Id] = HealthCheck.liftFBoolean {
  ReaderT(findUserByToken).map(_.nonEmpty)
}

def effToIO(token: Token): Eff ~> IO =
  λ[Eff ~> IO](_.run(token))

val route: Route = AkkaSup.healthCheckRoutesWithContext(checkReader) { request =>
  val token = request.headers.find(_.is("B3-Trace-Id")).fold("")(_.value)

  effToIO(token).andThen(λ[IO ~> Future](_.unsafeToFuture()))
}
```
