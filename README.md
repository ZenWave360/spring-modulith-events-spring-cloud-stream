# Spring-Modulith Events Externalizer for Spring Cloud Stream

[![Maven Central](https://img.shields.io/maven-central/v/io.zenwave360.sdk/spring-modulith-events-scs.svg?label=Maven%20Central&logo=apachemaven)](https://search.maven.org/artifact/io.zenwave360.sdk/spring-modulith-events-scs)
[![build](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/workflows/Build%20and%20Publish%20Maven%20Snapshots/badge.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/publish-maven-snapshots.yml)
[![coverage](https://raw.githubusercontent.com/ZenWave360/spring-modulith-events-spring-cloud-stream/badges/jacoco.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/build.yml)
[![branches coverage](https://raw.githubusercontent.com/ZenWave360/spring-modulith-events-spring-cloud-stream/badges/branches.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/build.yml)
[![GitHub](https://img.shields.io/github/license/ZenWave360/spring-modulith-events-spring-cloud-stream)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/blob/main/LICENSE)

Spring-Modulith Events Externalizer that uses Spring Cloud Stream supporting both JSON and Avro serialization formats.

Check out the blog post here: https://www.zenwave360.io/posts/Spring-Modulith-Events-Spring-Cloud-Stream-Externalizer/
Companion sample project: https://github.com/EDALearn/EDA-TransactionalOutbox-Modulith-JPA

## Getting Started

### Dependency
Add the following Maven dependency to your project:

```xml
<dependency>
    <groupId>io.zenwave360.sdk</groupId>
    <artifactId>spring-modulith-events-scs</artifactId>
    <version>${spring-modulith-events-scs.version}</version>
</dependency>
```

## Versions

This project was built and tested with the following versions:

| spring-modulith-events-scs | Spring Modulith | Spring Boot | Spring Cloud | SCSt Schema   |
|----------------------------|-----------------|-------------|--------------|---------------|
| 1.0.x                      | 1.4.x           | 3.4.x       | 2024.0.0     | 2.2.1.RELEASE |
| 1.1.x                      | 2.0.x           | 4.0.x       | 2025.1.0     | 2.2.1.RELEASE |
| 1.2.x                      | 2.1.x           | 4.1.x       | 2025.1.0     | 2.2.1.RELEASE |
| 1.3.x                      | 2.1.x           | 4.1.x       | 2025.1.0     | 5.0.x ¹       |

¹ Starting with 1.3.x, Avro integration tests use `org.springframework.cloud:spring-cloud-stream-schema-registry-client`
(its version is managed by the Spring Cloud BOM), which replaces the discontinued `spring-cloud-stream-schema` artifact.
The SCSt Schema column lists the Avro message converter library used in tests. It is not a dependency of this library.

Version 1.3.0 requires Jackson 3 (`tools.jackson.*`). It uses the application's
`tools.jackson.databind.json.JsonMapper` bean when one is present (e.g. provided by Spring Boot via
`spring-boot-starter-jackson`), and otherwise falls back to a default `JsonMapper`, so a Jackson
customization only applies to event serialization if it is exposed as a `JsonMapper` bean. Jackson 2's `com.fasterxml.jackson.databind.ObjectMapper`
is no longer supported. Applications that still use `spring-boot-jackson2` or provide only a
Jackson 2 `ObjectMapper` should remain on the 1.2.x line or migrate their Jackson configuration
before upgrading.

### Configuration
Use `@EnableSpringCloudStreamEventExternalization` annotation to enable Spring Cloud Stream event externalization in your Spring configuration:

```java
@Configuration
@EnableSpringCloudStreamEventExternalization
public class SpringCloudStreamEventsConfig {
    // Additional configurations (if needed)
}
```

This configuration ensures that, in addition to events annotated with `@Externalized`, all events of type `org.springframework.messaging.Message` with a header named `SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_EVENT_HEADER` will be externalized and routed to their specified destination using the value of this header as the routing target.

## Event Serialization

Using the transactional event publication log requires serializing events to a format that can be stored in a database. Since the generic type of `Message<?>` payload is lost when using the default `JacksonEventSerializer`, this library adds an extra `_class` field to preserve payload type information, allowing for complete deserialization to its original type.

This library provides support for POJO (JSON) and Avro serialization formats for `Message<?>` payloads.

### Avro Serialization

Avro serialization needs `tools.jackson.dataformat.avro.AvroMapper` on the classpath. To use Avro serialization, add the following dependency to your project:

```xml
<dependency>
    <groupId>tools.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-avro</artifactId>
</dependency>
```

Both Apache Avro 1.11.x and 1.12.x are supported. This library is intentionally built and tested against Avro 1.11.x as its baseline, so projects that have not yet moved to 1.12 can keep using it.

---

## Routing Events

### Programmatic Routing for `Message<?`> events

You can define routing targets programmatically using a Message header:

```java
public class CustomerEventsProducer implements ICustomerEventsProducer {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void onCustomerCreated(CustomerCreated event) {
        Message<CustomerCreated> message = MessageBuilder.withPayload(event)
                .setHeader(
                        SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_SENDTO_DESTINATION_HEADER, 
                        "customer-created") // <- target binding name
                .build();
        applicationEventPublisher.publishEvent(message);
    }
}
```

### Annotation-Based Routing for POJO Events

Leverage the `@Externalized` annotation to define the target binding name and routing key:

```java
@Externalized("customer-created::#{#this.getLastname()}")
class CustomerCreated {

    public String getLastname() {
        // Return the customer's last name
    }
}
```

### Configure Spring Cloud Stream destination

Configure Spring Cloud Stream destination for your bindings as usual in `application.yml`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        customer-created:
          destination: customer-created-topic
```

### Routing Key

`SpringCloudStreamEventExternalizer` dynamically sets the appropriate Message header (e.g., `kafka_messageKey` or `rabbit_routingKey`) from your routing key based on the channel binder type, if the routing header is not already present.

- KafkaMessageChannelBinder: `kafka_messageKey`
- RabbitMessageChannelBinder: `rabbit_routingKey`
- KinesisMessageChannelBinder: `partitionKey`
- PubSubMessageChannelBinder: `pubsub_orderingKey`
- EventHubsMessageChannelBinder: `partitionKey`
- SolaceMessageChannelBinder: `solace_messageKey`
- PulsarMessageChannelBinder: `pulsar_key`

---

## Using Snapshot Versions
In order to test snapshot versions of this library, add the following repository to your Maven configuration:

```xml
<repository>
    <id>maven-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
