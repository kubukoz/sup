---
layout: docs
title: redis4cats
---

sup has a <a href="https://redis4cats.profunktor.dev" target="_blank">redis4cats</a> module:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("redis")
```

Imports:
```scala mdoc:silent
import sup.modules.redis._
```

## What's included

### `pingCheck`

You can build a connection check out of a `Ping` algebra (included in `RedisCommands`). Let's create one first:

```scala mdoc
import dev.profunktor.redis4cats.algebra.Ping, cats.effect._

implicit def ping: Ping[IO] = ???
```

And now the health check:

```scala mdoc
def redisCheck = pingCheck[IO, Throwable]
```

Errors are automatically recovered to a `Sick` status in this check.
