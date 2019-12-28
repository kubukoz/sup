---
layout: docs
title: redis4cats
---

sup has a redis4cats module:

```tut:passthrough
sup.microsite.sbtDependencies("redis")
```

Imports:
```tut:silent
import sup._, sup.modules.redis._
```

## What's included

### `pingCheck`

You can build a connection check out of a `Ping` algebra (included in `RedisCommands`). Let's create one first:

```tut:book
import dev.profunktor.redis4cats.algebra.Ping, cats.effect._

implicit def ping: Ping[IO] = ???
```

And now the health check:

```tut:book
def redisCheck = pingCheck[IO, Throwable]
```

Errors are automatically recovered to a `Sick` status in this check.
