# spring-cloud-sleuth-otel-webflux-baggage

The purpose of this repository is to
reproduce [Baggage is not propagated to MDC](https://github.com/spring-projects-experimental/spring-cloud-sleuth-otel/issues/122)
issue in [spring-cloud-sleuth-otel](https://github.com/spring-projects-experimental/spring-cloud-sleuth-otel) library

# How to run it?

- Compile and run with `maven`
- Hit [http://localhost:8080](http://localhost:8080) endpoint
- Have a look at logs in console

> **Note:** There is another branch, which uses the second approach, with `WebFluxSleuthOperators` utility class.
