---
layout: docs
title: log4cats
---

sup has a <a href="https://christopherdavenport.github.io/log4cats" target="_blank">log4cats</a> module:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("log4cats")
```

Imports:
```scala mdoc:silent
import sup._, sup.modules.log4cats._
```

## What's included

### `logged`

The log4cats module of sup provides a simple modifier that logs a message before the check
and a message with the result of the healthcheck:

```scala mdoc
import cats._, cats.implicits._, cats.data._
import org.typelevel.log4cats.Logger, org.typelevel.log4cats.extras._

val healthCheck: HealthCheck[Id, Id] = HealthCheck.const(Health.Healthy)

implicit val logger: Logger[Writer[Chain[LogMessage], *]] = WriterLogger()

val loggedCheck = healthCheck.leftMapK(WriterT.liftK[Id, Chain[LogMessage]]).through(logged("foo"))

val (logs, result) = loggedCheck.check.run.leftMap(_.toList)
```
