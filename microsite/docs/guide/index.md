---
layout: docs
title: Usage guide
---

# Usage guide

- [Usage guide](#usage-guide)
  - [Installation](#installation)
  - [Core concepts](#core-concepts)
    - [Health](#health)
    - [HealthResult](#healthresult)
    - [HealthCheck](#healthcheck)
  - [Advanced concepts](#advanced-concepts)
    - [HealthReporter](#healthreporter)
    - [Tagging](#tagging)
  - [Modifiers](#modifiers)
    - [`timeoutToSick`](#timeouttosick)
    - [`tagWith` / `untag`](#tagwith--untag)
    - [`recoverToSick`](#recovertosick)
    - [`surround`](#surround)

## Installation

For sbt:

```scala mdoc:passthrough
sup.microsite.sbtDependencies("core")
```

For ammonite:

```scala mdoc:passthrough
sup.microsite.ammDependency("core")
```

Imports:
```scala mdoc:silent
import sup._
```

## Core concepts

- [Health](#health)
- [HealthResult](#healthresult)
- [HealthCheck](#healthcheck)

### Health

`Health` is a boolean-like health status. In ADT notation:

```haskell
data Health = Sick | Healthy
```

It has a commutative monoid, which is equivalent to the "all" monoid for booleans.

That means `Health` values can be combined with the following semantics:

| \|+\|       | **Sick** | **Healthy** |
| ----------- | -------- | ----------- |
| **Sick**    | Sick     | Sick        |
| **Healthy** | Sick     | Healthy     |
| ---         | -----    | ------      |


### HealthResult

```scala
//simplified
type HealthResult[H[_]] = H[Health]
```

`HealthResult` is a wrapper over a collection `H` of `Health`.
There are no limitations about what kind of collection that must be, but it's recommended
that it has a `cats.Foldable` instance. For a single Health, `cats.Id` can be used.

Other examples of a suitable type include:

- `cats.Id`: there's only one result.
- `sup.Tagged[String, *]`: there's only one result, tagged with a String (e.g. the dependency's name)
- `cats.data.NonEmptyList`: there are multiple checks
- `sup.data.Report[cats.Id, cats.data.NonEmptyList, *]`: there's one check, and a `NonEmptyList` of checks
- `sup.data.Report[sup.Tagged[String, *], NonEmptyList, *]`: there's one check, and a `NonEmptyList` of checks tagged with a `String`.

(`sup.data.Report[F, G, *]` is equivalent to `cats.data.OneAnd[Nested[F, G, *], *]`)

`HealthResult[H]` has a `Monoid` for any `H[_]: Applicative`, although most of its usages will be transparent to the user.

### HealthCheck

```scala
//simplified
trait HealthCheck[F[_], H[_]] {
  def check: F[HealthResult[H]]
}
```

`HealthCheck[F, H]` is a health-checking action with effects of type `F` that'll result in a collection `H` of `Health`.

Similarly to `HealthResult`, a `HealthCheck[F, H]` has a monoid for any `F[_]: Applicative, H[_]: Applicative`.
This is really cool, because thanks to this we can combine two similar healhchecks into one that'll check both
(sequentially or in parallel, depending on the applicative of F - to enforce parallel execution use `HealthCheck.parTupled`).

Let's start with some cats imports (assume they're available in the rest of the page):

```scala mdoc:silent
import cats._, cats.data._, cats.effect._, cats.implicits._, cats.effect.unsafe.implicits._
```

and here's how healthchecks can be combined:

```scala mdoc
//will always be Sick
def queue1: HealthCheck[IO, Id] = HealthCheck.const(Health.Sick)

//will always be Healthy
def queue2: HealthCheck[IO, Id] = HealthCheck.const(Health.Healthy)

//will always be Sick
def queues = queue1 |+| queue2
```

## Advanced concepts

In sup, a single dependency's healthcheck has the same underlying structure as a whole service's healthcheck
consisting of multiple dependencies' checks.

### HealthReporter

A healthcheck wrapping multiple healthchecks is called a `HealthReporter`. Here's how it's defined in sup:

```scala
import sup.data.Report

type HealthReporter[F[_], G[_], H[_]] = HealthCheck[F, Report[G, H, *]]
```

You can construct one from a sequence of healthchecks using the `HealthReporter.fromChecks` function:

```scala mdoc
import sup.data.HealthReporter //Import actual HealthReporter

val kafka: HealthCheck[IO, Id] = HealthCheck.const(Health.Healthy)
val postgres: HealthCheck[IO, Id] = HealthCheck.const(Health.Healthy)

val reporter: HealthReporter[IO, NonEmptyList, Id] = HealthReporter.fromChecks(kafka, postgres)
```

### Tagging

A healthcheck can be tagged with a label, e.g. a `String` with the dependency's name:

```scala mdoc
val kafkaTagged = kafka.through(mods.tagWith("kafka"))
val postgresTagged = postgres.through(mods.tagWith("postgres"))

val taggedReporter = HealthReporter.fromChecks(kafkaTagged, postgresTagged)
```

## Modifiers

sup provides a variety of ways to customize a healthcheck. These include `through`, `mapK`, `transform`, `mapResult` and `leftMapK`.
Check out the predefined modifiers in the `sup.mods` object or create your own.

Here are some example modifiers provided by sup:

### `timeoutToSick`

A check modified with `timeoutToSick` will be marked as sick if it doesn't complete within the given duration.

```scala mdoc
import scala.concurrent.duration._

val timedKafka = kafka.through(mods.timeoutToSick(5.seconds))
```

Other modifiers with timeouts include `timeoutToDefault` and `timeoutToFailure`.

### `tagWith` / `untag`

Tag a healthcheck with a value (or unwrap a tagged healthcheck):

```scala mdoc
val taggedKafka2 = kafka.through(mods.tagWith("foo"))

val untaggedKafka = taggedKafka2.through(mods.untag)
```

### `recoverToSick`

Swallows any errors that might happen in the healthcheck's effect and falls back to `Sick`.

```scala mdoc
val safeKafka = kafka.through(mods.recoverToSick)
```

### `surround`

Surrounds a healthcheck with effectful actions.

```scala mdoc
val surroundedKafka = kafka.through(mods.surround(IO(println("foo")))(result => IO(println(s"foo result: $result"))))
```
