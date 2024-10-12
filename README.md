# k3a-topic-terminator

Application to terminate unused topics in Kafka

:warning: **WARNING: This project is really fresh! Use with caution!**

:exclamation: NOTE: For now, dry-run is enabled by default, so to delete
any unused topics, dry-run must be explicitly disabled.

The goal is to terminate (delete) unused topics in Kafka
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

The application is a simple spring-boot application with a scheduled
cleanup-job that will run at a fixed (configurable) rate.
The job will query the configured Kafka cluster for unused topics
and delete (or just log; if dry-run enabled) the detected unused topics.

## Configuration
