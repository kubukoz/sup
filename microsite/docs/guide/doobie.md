---
layout: docs
title: doobie
---

sup has a <a href="https://tpolecat.github.io/doobie" target="_blank">doobie</a> module:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("doobie")
```

Imports:
```scala mdoc:silent
import sup.modules.doobie._
```

## What's included

### `connectionCheck`

You can build a connection check out of a doobie `Transactor`. Let's create one first:

```scala mdoc
import doobie._, cats.effect._

def transactor: Transactor[IO] = ???
```

And now the health check:

```scala mdoc
import scala.concurrent.duration._

def doobieCheck = connectionCheck(transactor)(timeout = Some(5.seconds))
```

