---
layout: docs
title: http4s-circe
---

sup has modules for <a href="https://http4s.org" target="_blank">http4s</a>, http4s-client and <a href="https://circe.github.io/circe" target="_blank">circe</a>:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("http4s", "http4s-client", "circe")
```

Imports:
```scala mdoc:silent
import sup._
import sup.modules.http4s._, sup.modules.http4sclient._, sup.modules.circe._
```

## What's included

### `statusCodeHealthCheck`, `healthCheckRoutes`, `Encoder`/`Decoder`

In the http4s module, you get a router for (almost) free.
You just need some instances, a healthcheck and an `EntityEncoder[F, HealthResult[H]]`.

The circe module provides circe `Encoder`/`Decoder` instances for all the important bits,
so you can use them together with `http4s-circe` and bundle the whole thing up:

```scala mdoc
import org.http4s.circe.CirceEntityCodec._, org.http4s._, org.http4s.implicits._, org.http4s.client._
import cats.effect.unsafe.implicits._
import cats.effect._, cats._, cats.data._, sup.data._

implicit val client: Client[IO] = Client.fromHttpApp(HttpApp.notFound[IO])

val healthcheck: HealthCheck[IO, Id] = statusCodeHealthCheck(Request[IO]())

val routes = healthCheckRoutes(healthcheck)

//also works with reports!

val report = HealthReporter.fromChecks(
  healthcheck.through(mods.tagWith("foo")),
  HealthCheck.const[IO, Id](Health.Healthy).through(mods.tagWith("bar"))
)

val reportRoutes = healthCheckRoutes(report)

val responseBody = reportRoutes.orNotFound.run(Request(uri = Uri.uri("/health-check"))).flatMap(_.bodyText.compile.string)
println(responseBody.unsafeRunSync())
```

### `remoteHealthCheck`

There's a way to build a healthcheck out of a request to a remote one:

```scala mdoc
val remoteCheck = remoteHealthCheck[IO, Id](Request())
```
