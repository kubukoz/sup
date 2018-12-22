---
layout: docs
title: Usage guide
---

# Usage guide

* [Installation](#installation)
* [Main concepts](#main-concepts)
  + [Health](#health)
  + [HealthResult](#healthresult)
  + [HealthCheck](#healthcheck)
* [Advanced concepts](#advanced-concepts)
  + [HealthReporter](#healthreporter)
  + [Tagging](#tagging)
* [Modifiers](#modifiers)
  
## Installation

For sbt:

```
libraryDependencies += "com.kubukoz" %% "sup-core" % "0.1.0"
```

For ammonite:

```
import $ivy.`com.kubukoz::sup-core:0.1.0`
```

Imports:
```tut:silent
import sup._
```

## Main concepts

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

\|+\| | **Sick** | **Healthy**
---|------|---------
**Sick** | Sick | Sick
**Healthy** | Sick | Healthy
---|-----|------


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
- `sup.Tagged[String, ?]`: there's only one result, tagged with a String (e.g. the dependency's name)
- `cats.data.NonEmptyList`: there are multiple checks
- `cats.data.OneAnd[cats.data.NonEmptyList, ?]`: there's one check, and a `NonEmptyList` of checks
- `cats.data.OneAnd[sup.TaggedNel[String, ?], ?]`: there's one check, and a `NonEmptyList` of checks tagged with a `String`.

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
This is really cool, because thanks to this we can combine two similar healhchecks into one that'll check both.

Let's start with some cats imports (assume they're available in the rest of the page):

```tut:silent
import cats._, cats.data._, cats.effect._, cats.implicits._
```

and here's how healthchecks can be combined:

```tut:book
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
import cats.data.OneAnd

type HealthReporter[F[_], G[_]] = HealthCheck[F, OneAnd[G, ?]]
```

You can construct one from a sequence of healthchecks using the `HealthReporter.fromChecks` function:

```tut:book
val kafka: HealthCheck[IO, Id] = HealthCheck.const(Health.Healthy)
val postgres: HealthCheck[IO, Id] = HealthCheck.const(Health.Healthy)

val reporter: HealthReporter[IO, NonEmptyList] = HealthReporter.fromChecks(kafka, postgres)
```

### Tagging  

A healthcheck can be tagged with a label, e.g. a `String` with the dependency's name:

```tut:book
import sup.mods._

val kafkaTagged = kafka.mapK(mods.tagWith("kafka"))
val postgresTagged = postgres.mapK(mods.tagWith("postgres"))

val taggedReporter = HealthReporter.fromChecks(kafkaTagged, postgresTagged)
```

## Modifiers

sup provides a variety of ways to customize a healthcheck. These include `mapK`, `transform`, `mapResult` and `leftMapK`
(the method that fits a modifier best is mentioned in each modifier's Scaladoc).
Check out the predefined modifiers in the `sup.mods` object or create your own.

Here are some example modifiers provided by sup:

### `timeoutToSick`

A check modified with `timeoutToSick` will be marked as sick if it doesn't complete within the given duration.

```tut:book
import scala.concurrent.duration._

implicit val contextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
implicit val timer: Timer[IO] = IO.timer(scala.concurrent.ExecutionContext.global)
val timedKafka = kafka.transform(mods.timeoutToSick(5.seconds))
```

Other modifiers with timeouts include `timeoutToDefault` and `timeoutToFailure`.

### `tagWith` / `untag`

Tag a healthcheck with a value (or unwrap a tagged healthcheck):

```tut:book
val taggedKafka2 = kafka.mapK(mods.tagWith("foo"))
```
