---
layout: docs
title: http4s-circe
---

sup has modules for http4s, http4s-client and circe:

```tut:passthrough
sup.microsite.sbtDependencies("http4s", "http4s-client", "circe")
```

Imports:
```tut:silent
import sup._
import sup.modules.http4s._, sup.modules.http4sclient._, sup.modules.circe._
```

## What's included

### `statusCodeHealthCheck`, `healthCheckRoutes`, `Encoder`/`Decoder`

In the http4s module, you get a router for (almost) free.
You just need some instances, a healthcheck and an `EntityEncoder[F, HealthResult[H]]`.

The circe module provides circe `Encoder`/`Decoder` instances for all the important bits,
so you can use them together with `http4s-circe` and bundle the whole thing up:

```tut:book
import org.http4s.circe._, org.http4s._, org.http4s.client._, cats.effect._, cats._

implicit val client: Client[IO] = Client.fromHttpApp(HttpApp.notFound[IO])
 
implicit val encoder: EntityEncoder[IO, HealthResult[Id]] = jsonEncoderOf

val healthcheck: HealthCheck[IO, Id] = statusCodeHealthCheck(Request[IO]())

val routes = healthCheckRoutes(healthcheck)
```

### `remoteHealthCheck`

There's a way to build a healthcheck out of a request to a remote one:

```tut:book
implicit val decoder: EntityDecoder[IO, HealthResult[Id]] = jsonOf

val remoteCheck = remoteHealthCheck(Request[IO]())
```
