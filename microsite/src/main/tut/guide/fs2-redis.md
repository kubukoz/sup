---
layout: docs
title: fs2-redis
---

sup has an fs2-redis module:

```
libraryDependencies += "com.kubukoz" %% "sup-redis" % "0.1.0"
```

Imports:
```tut:silent
import sup._, sup.redis._
```

## What's included

### `pingCheck`

You can build a connection check out of a `Ping` algebra (included in `RedisCommands`). Let's create one first:

```tut:book
import com.github.gvolpe.fs2redis.algebra.Ping, cats.effect._

implicit def ping: Ping[IO] = ???
```

And now the health check:

```tut:book
def doobieCheck = pingCheck[IO, Throwable]
```

Errors are automatically recovered to a `Sick` status in this check.
