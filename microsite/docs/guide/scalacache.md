---
layout: docs
title: ScalaCache
---

sup has a <a href="https://cb372.github.io/scalacache" target="_blank">ScalaCache</a> module:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("scalacache")
```

Imports:
```scala mdoc:silent
import sup._, sup.modules.scalacache._
```

## What's included

### `cached`

You can make a healthcheck cached by transforming it with the `cached` function
(which is a pretty thin wrapper over `cache.cachingForMemoizeF(key)(ttl)`, so you can just use that).

Let's grab a bunch of imports and define our Scalacache config:

```scala mdoc
import cats._, cats.effect._, cats.data._, scala.concurrent.duration._, scalacache.{Id => _, _}, sup.mods._, sup.data._

//you'll probably want to implement these
implicit def cache[H[_]]: Cache[HealthResult[H]] = ???
implicit def mode[F[_]]: Mode[F] = ???
```

Now, the health check:

```scala mdoc
def queueCheck(queueName: String): HealthCheck[IO, Tagged[String, ?]] =
  HealthCheck.const[IO, Id](Health.Healthy).through(tagWith(queueName))

def q1 = queueCheck("foo").through(cached("queue-foo", Some(10.seconds)))
```

Because a `HealthReporter` is just a special case of `HealthCheck`, the same modifier works for reporters:

```scala mdoc
def reporter: HealthReporter[IO, NonEmptyList, Tagged[String, ?]] =
  HealthReporter.fromChecks(
    queueCheck("foo").through(cached("queue-foo", Some(10.seconds))),
    queueCheck("bar")
  ).through(cached("report", Some(5.seconds)))
```

The above will be a `HealthReporter` that'll cache the whole system's health for 5 seconds,
also caching the first queue's health for 10 seconds.
