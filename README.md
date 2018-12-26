# sup

[![Join the chat at https://gitter.im/sup-scala/community](https://badges.gitter.im/sup-scala/community.svg)](https://gitter.im/sup-scala/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.com/kubukoz/sup.svg?branch=master)](https://travis-ci.com/kubukoz/sup)
[![Latest version](https://index.scala-lang.org/kubukoz/sup/sup-core/latest.svg)](https://index.scala-lang.org/kubukoz/sup/sup-core)
[![Maven Central](https://img.shields.io/maven-central/v/com.kubukoz/sup-core_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Csup-core)
[![License](http://img.shields.io/:license-Apache%202-green.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

[![Powered by cats](https://img.shields.io/badge/powered%20by-cats-blue.svg)](https://github.com/typelevel/cats)
![Gluten free](https://img.shields.io/badge/gluten-free-orange.svg)


'sup (/sʌp/) provides composable, purely functional healthchecks.

```
"com.kubukoz" %% "sup-core" % supVersion
```

Check out [the microsite](https://sup.kubukoz.com) and [the usage guide](https://sup.kubukoz.com/guide). 

## Why?

If your application has any external dependencies, its ability to work properly relies on the availability of these dependencies.
Most applications communicate with at least one database, and sometimes a service provided by someone else (e.g. Amazon S3, PayPal and so on).
It's also common to talk to a message broker (Kafka, RabbitMQ, ActiveMQ, etc.). In the microservice world, your applications will probably talk to each other, as welll.

The last thing you want to happen is some other system's problem causing downtime in yours.
We're living in a world where [the network isn't reliable](https://en.wikipedia.org/wiki/Network_partition),
and even healthy services can fail to respond to your requests due to network issues at any time.

By relying on systems living beyond your application's control, you reduce your [SLA](https://en.wikipedia.org/wiki/Service-level_agreement)
to the lowest SLA of your dependencies. Even if your application has nine nines of availability (99.9999999%) on its own,
but it requires S3 (with uptime of 99.9%) to be up, your application is only available for 99.9% of the time.
 
Because of the risk associated with external services, modern applications employ a range of fault tolerance and isolation mechanisms,
like circuit breakers, rate limiters and bulkheads.
To ensure that our applications handle failure properly, and that we can react to problems in the system
knowing what exactly doesn't work, we also track its health by
[monitoring](https://docs.microsoft.com/en-us/azure/architecture/best-practices/monitoring), tracing and other diagnostics.

We also use healthchecks.

## Health checks

The health check pattern usually involves having an API endpoint that reports the health of your application,
including the health of each external dependency. It's reasonable that the information the endpoint exposes be cached
and automatically refreshed, and protected by a circuit breaker to ensure that checking the health
doesn't make matters worse.

## Goals of this project

The main goal of sup is to provide a reusable model for working with health checks, as well as
a range of utilities for customizing them by e.g. adding timeouts or error recovery.
It also provides implementations for health checks of some popular integrations (JDBC, HTTP, etc.).

It's a design decision not to include any sort of metrics, response times, version information or other statistics
in sup - they are simply beyond the scope of health checks.
Although some of these can be implemented by users and used with sup, they're not officially supported.

Another design decision is that health is binary - a service is either healthy or not,
and there's no "unknown" state.
