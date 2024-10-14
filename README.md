# k3a-topic-terminator

Application to terminate unused topics in Kafka

:warning: **WARNING: This project is really fresh! Use with caution!**

:exclamation: NOTE: For now, dry-run is enabled by default, so to delete
any unused topics, dry-run must be explicitly disabled.

## Goal

The goal is to terminate (delete) unused topics in Kafka.
Unused topics are topics that match the following three conditions:

- Not internal topic used internally by Kafka components
- Topic without consumers
  - Inactive consumers are eventually cleaned up automatically by Kafka
- Topic that is empty

## Origins

This project is heavily inspired by
[kafka-topic-cleaner](https://github.com/HungUnicorn/kafka-topic-cleaner)
but that project seems abandoned for a while,
so we decided to create something new.

## Architecture

The application is a simple Spring Boot application with a scheduled
cleanup-job that will run at a fixed (configurable) rate.
The job will query the configured Kafka cluster for unused topics
and delete (or just log; if dry-run enabled) the found unused topics.

## Configuration

As a Spring Boot application almost any aspect of the application runtime can be
customized.
The defaults should normally be sufficient, but if required please consult the
[Spring Boot common application properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html)
for reference.

The application provides a
[Prometheus endpoint](https://docs.spring.io/spring-boot/reference/actuator/metrics.html#actuator.metrics.export.prometheus)
with all application metrics enabled by default.

Configuration using environment variables is recommended.
Please see
[Spring Boot externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
to understand how to configure Spring Boot application properties using
environment variables. 

### Kafka cluster configuration

The application uses the Kafka admin client provided by
Spring for Apache Kafka to interact with the Kafka cluster.
Available properties are properties starting with `spring.kafka` in
[Spring Boot integration properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#appendix.application-properties.integration).

As minimum, the `spring.kafka.bootstrap-servers` must be set.
Example set as environment variable:

```
SPRING_KAFKA_BOOTSTRAP_SERVERS=broker-1:9091,broker-2:9091,broker-3:9091
```

### Application configuration

The application provides a few application-specific properties.
See
[ApplicationProperties](src/main/java/io/statnett/k3a/topicterminator/ApplicationProperties.java)
for an overview and
[application.yaml](src/main/resources/application.yaml)
for the current default values.
