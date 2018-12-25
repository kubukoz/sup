---
layout: docs
title: Doobie
---

sup has a Doobie module:

```tut:passthrough
sup.microsite.sbtDependencies("doobie")
```

Imports:
```tut:silent
import sup._, sup.modules.doobie._
```

## What's included

### `connectionCheck`

You can build a connection check out of a doobie `Transactor`. Let's create one first:

```tut:book
import doobie._, cats.effect._

def transactor: Transactor[IO] = ???
```

And now the health check:

```tut:book
import eu.timepit.refined.auto._
def doobieCheck = connectionCheck(transactor)(timeoutSeconds = Some(5))
```
