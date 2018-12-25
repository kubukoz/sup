---
layout: docs
title: http4s-circe
---

sup has modules for http4s, http4s-client and circe:

```
libraryDependencies += Seq(
  "com.kubukoz" %% "sup-http4s" % "0.1.0",
  "com.kubukoz" %% "sup-http4s-client" % "0.1.0",
  "com.kubukoz" %% "sup-circe" % "0.1.0"
)
```

Imports:
```tut:silent
import sup._, sup.http4s._, sup.http4sclient._, sup.circe._
```

## What's included

In the http4s module, you get a router for (almost) free.
You just need some instances, a healthcheck and an `EntityEncoder[F, HealthResult[H]]`.

The circe module provides circe `Encoder` instances for all the important bits,
so you can use them together with `http4s-circe` and bundle the whole thing up:

```tut:book
import org.http4s.circe._, org.http4s._, org.http4s.client._, cats.effect._, cats._

implicit val client: Client[IO] = Client.fromHttpApp(HttpApp.notFound[IO])
 
implicit val encoder: EntityEncoder[IO, HealthResult[Id]] = jsonEncoderOf

val healthcheck: HealthCheck[IO, Id] = sup.http4sclient.healthCheck(Request[IO]())

val routes = healthCheckRoutes(healthcheck)
```
