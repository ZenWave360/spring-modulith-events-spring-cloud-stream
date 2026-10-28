## What's Changed

Release notes for the **1.3.1** release.

This is a maintenance release. It contains no functional changes to the library and no changes to the
format used to persist events in the Spring Modulith event publication log.

## Dependency Updates

### Spring Modulith 2.1.2 and Spring Boot 4.1.2

The library is now built and tested against Spring Modulith 2.1.2 (up from 2.1.0) and Spring Boot 4.1.2
(up from 4.1.0). Applications can upgrade to these versions without any change to their configuration.

## Verification

### End-to-end Sample Project

The library has been verified against the
[transactional-outbox-modulith-jpa](https://github.com/ZenWave360/zenwave-playground/tree/main/examples/transactional-outbox-modulith-jpa)
sample (Spring Boot 4.1.2, Spring Modulith 2.1.2, Spring Cloud 2025.1.4, Avro payloads generated from
AsyncAPI, PostgreSQL and Kafka). Its integration tests check that:

- an event published inside a transaction is externalized to Kafka through Spring Cloud Stream as an
  Avro record, with the message key and the AsyncAPI-defined headers;
- an Avro `Message<?>` survives serialization to, and deserialization from, the event publication log;
- incomplete publications are read back from the log, rebuilt as Avro and published again when they
  are resubmitted.

### Compatibility With Previously Stored Events

Events stored in the event publication log by **1.1.1** and **1.3.0** are still deserialized and
published correctly. Applications can upgrade with outstanding (incomplete) publications in their
`event_publication` table.

## Internal

- CI: snapshots are now published to the `zenwave360/maven-snapshots` repository using
  `git-maven-publisher`.
- Documentation: the README links to the new location of the companion sample project.

**Full Changelog**: https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/compare/v1.3.0...v1.3.1
