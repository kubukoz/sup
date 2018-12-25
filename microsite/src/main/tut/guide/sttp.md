---
layout: docs
title: sttp
---

sup has a module for sttp:

```
libraryDependencies += "com.kubukoz" %% "sup-sttp" % "0.1.0"
```

Imports:
```tut:silent
import sup._, sup.modules.sttp._
```

## What's included

### `statusCodeHealthCheck`

There's a way to build a healthcheck out of a request and a backend:

```tut:book
import com.softwaremill.sttp.{sttp => request, _}, cats.implicits._, cats.effect._

implicit def backend: SttpBackend[IO, Any] = ???
 
def check: HealthCheck[IO, Id] = statusCodeHealthCheck[IO, String](request.get(uri"https://google.com"))
```
