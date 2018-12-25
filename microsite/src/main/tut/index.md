---
layout: home
---
# sup

[![Build Status](https://travis-ci.com/kubukoz/sup.svg?branch=master)](https://travis-ci.com/kubukoz/sup)
[![Latest version](https://index.scala-lang.org/kubukoz/sup/sup-core/latest.svg)](https://index.scala-lang.org/kubukoz/sup/sup-core)
[![Maven Central](https://img.shields.io/maven-central/v/com.kubukoz/sup-core_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Csup-core)
[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

[![Powered by cats](https://img.shields.io/badge/powered%20by-cats-blue.svg)](https://github.com/typelevel/cats)
![Gluten free](https://img.shields.io/badge/gluten-free-orange.svg)

'sup (/sʌp/) provides composable, purely functional healthchecks.

## Installation

For sbt:

```tut:passthrough
sup.microsite.sbtDependencies("core")
```

For ammonite:

```tut:passthrough
sup.microsite.ammDependency("core")
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
