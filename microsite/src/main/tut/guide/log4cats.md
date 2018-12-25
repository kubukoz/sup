---
layout: docs
title: log4cats
---

sup has a log4cats module:

```tut:passthrough
sup.microsite.sbtDependencies("log4cats")
```

Imports:
```tut:silent
import sup._, sup.modules.log4cats._
```

## What's included

### `logged`

The log4cats module of sup provides a simple modifier that logs a message before the check
and a message with the result of the healthcheck:

```tut:book
import cats._, cats.implicits._, cats.effect._, cats.data._
import io.chrisdavenport.log4cats.Logger, io.chrisdavenport.log4cats.extras._
val healthCheck: HealthCheck[Id, Id] = HealthCheck.const(Health.Healthy)

implicit val logger: Logger[Writer[Chain[LogMessage], ?]] = WriterLogger()
 
val loggedCheck = healthCheck.leftMapK(WriterT.liftK[Id, Chain[LogMessage]]).through(logged("foo"))
  
val (logs, result) = loggedCheck.check.run
``` 
