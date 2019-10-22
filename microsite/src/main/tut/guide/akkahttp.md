---
layout: docs
title: akka-http
---

sup has modules for akka-http and circe (although you can use any JSON library compatible with akka-http):

```tut:passthrough
sup.microsite.sbtDependencies("akka-http", "circe")
```

Imports:
```tut:silent
import sup._
import sup.modules.akkahttp._, sup.modules.circe._
```

## What's included

### `healthCheckRoutes`, `healthCheckResponse`

It is possible to create a `Route` from a health check.

To do so, you'll need:

- a `cats.effect.Effect` instance for your health check's effect type
- a `Reducible` instance for your container of results
- and a way to serialize the results (as defined per the implicit `ToEntityMarshaller[HealthResult[H]]`).

Once you have all three, use `healthCheckRoutes`:

```tut:book
import cats.effect.IO, cats.Id
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

val check: HealthCheck[IO, Id] = HealthCheck.const(Health.healthy)

val routes: Route = healthCheckRoutes(check)
```

You can customize the path at which the health checks will be available (the default is `path("health-check")`).

Alternatively, you can get a raw response built from a check, inside the effect, using `healthCheckResponse` directly
and constructing your Route manually from that.


### `healthCheckRoutesWithContext`

If your health check's effect type requires information provided by the request (e.g. a tracing token header),
you can use `healthCheckRoutesWithContext` and provide a way to run the effect from a request.

For example:

```tut:book
import cats.data.ReaderT, cats.~>
import scala.concurrent.Future

case class User()
type Token = String

type Eff[A] = ReaderT[IO, Token, A]

def findUserByToken(token: Token): IO[Option[User]] = ???

val check: HealthCheck[Eff, Id] = HealthCheck.liftFBoolean {
  ReaderT(findUserByToken).map(_.nonEmpty)
}

def effToIO(token: Token): Eff ~> IO =
  λ[Eff ~> IO](_.run(token))

val route: Route = healthCheckRoutesWithContext(check) { request =>
  val token = request.headers.find(_.is("B3-Trace-Id")).fold("")(_.value)

  effToIO(token).andThen(λ[IO ~> Future](_.unsafeToFuture()))
}
```
