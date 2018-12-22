---
layout: home
---
# sup

[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

'sup (/sʌp/) provides composable, purely functional healthchecks.

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

## Usage

Check out [the usage guide](guide). 

## Compatibility

sup is cross-published for Scala 2.11 and 2.12.

The core module has a dependency on cats-core 1.x and cats-effect 1.x.
