---
layout: docs
title: sttp
---

sup has a module for <a href="https://sttp.readthedocs.io/en/latest" target="_blank">sttp</a>.

```scala mdoc:passthrough
sup.microsite.sbtDependencies("sttp")
```

Imports:
```scala mdoc:silent
import sup._, sup.modules.sttp._
```

## What's included

### `statusCodeHealthCheck`

There's a way to build a healthcheck out of a request and a backend:

```scala mdoc
import sttp.client3._, cats.effect._

implicit def backend: SttpBackend[IO, Nothing] = ???

def check: HealthCheck[IO, cats.Id] =
  statusCodeHealthCheck[IO, Either[String, String]](basicRequest.get(uri"https://google.com"))
```
