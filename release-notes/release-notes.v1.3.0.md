## What's Changed

Release notes for the **1.3.0** release.

## What's New

### Native Jackson 3 Support

`spring-modulith-events-scs` now uses Jackson 3 throughout its custom event serialization.
`MessageEventSerializer` uses `tools.jackson.databind.ObjectMapper`, and its Spring configuration
now consumes the `tools.jackson.databind.json.JsonMapper` configured by Spring Boot 4. This removes
the need to add the deprecated `spring-boot-jackson2` compatibility module or manually define a
Jackson 2 `ObjectMapper` bean.

A `JsonMapper` bean is no longer required. When the application exposes one (for example through
`spring-boot-starter-jackson`), the event serializer uses it, including any customizations. When
none is available, the auto-configuration falls back to a default `JsonMapper` instead of failing
at startup. Custom Jackson settings apply to event serialization only if they are exposed as a
`JsonMapper` bean.

The custom serializer continues to preserve the concrete payload type and message header types
when persisting `Message<?>` events in Spring Modulith's transactional event publication log.

### Jackson 3 Avro Support

Avro event serialization now uses `tools.jackson.dataformat:jackson-dataformat-avro` and
`tools.jackson.dataformat.avro.AvroMapper`. JSON and generated Avro event payloads have both been
verified with Spring Boot 4.1 and Spring Modulith 2.1.

### Apache Avro 1.11.x Baseline

Apache Avro 1.11.x remains the intentionally supported baseline, and the library is built and
tested against Avro 1.11.4. Avro 1.12.x is also supported and verified, so applications can keep
their current Avro version and upgrade to 1.12 on their own schedule.

## Minor Breaking Changes

### Jackson 2 Compatibility Removed

Version 1.3.0 requires Jackson 3 (`tools.jackson.*`). The serializer constructors and Spring
configuration no longer accept Jackson 2's `com.fasterxml.jackson.databind.ObjectMapper`.

Applications using `spring-boot-jackson2`, or applications that define only a Jackson 2
`ObjectMapper` bean, should migrate their mapper configuration to Jackson 3 before upgrading. These
applications will still start, but the Jackson 2 mapper is ignored and events are serialized with a
default Jackson 3 `JsonMapper`, so Jackson 2 customizations (modules, naming strategies, date
formats) no longer apply to the event publication log. Users that must remain on Jackson 2 should
continue using the 1.2.x release line.

Avro applications must also replace `com.fasterxml.jackson.dataformat:jackson-dataformat-avro`
with `tools.jackson.dataformat:jackson-dataformat-avro`.

## Build and Test Changes

### Spring Cloud Stream Schema Registry Client

The Avro integration tests now use `org.springframework.cloud:spring-cloud-stream-schema-registry-client`
(5.0.x, managed by the Spring Cloud 2025.1 BOM) instead of the discontinued
`spring-cloud-stream-schema:2.2.1.RELEASE`. Only the package of `AvroSchemaMessageConverter` changed
(`org.springframework.cloud.stream.schema.registry.avro`). This is a test-scoped dependency and does
not affect the library's runtime dependencies. Applications that still use the old artifact for
Avro message conversion are encouraged to migrate as well.

The old artifact brought in `spring-boot-starter-web`, and with it Spring Boot's Jackson
auto-configuration, as a transitive test dependency. The integration tests now run without a
Spring Boot `JsonMapper` bean, so they exercise the default `JsonMapper` fallback described above.
The suite passes with both Avro 1.11.4 and 1.12.1.

**Full Changelog**: https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/compare/v1.2.0...v1.3.0
