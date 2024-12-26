# ZenWave Spring-Modulith Events Externalizer for Spring Cloud Stream

[![Maven Central](https://img.shields.io/maven-central/v/io.zenwave360.sdk/spring-modulith-events-scs.svg?label=Maven%20Central&logo=apachemaven)](https://search.maven.org/artifact/io.zenwave360.sdk/spring-modulith-events-scs)
[![build](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/workflows/Build/badge.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/build.yml)
[![coverage](https://raw.githubusercontent.com/ZenWave360/spring-modulith-events-spring-cloud-stream/badges/jacoco.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/build.yml)
[![branches coverage](https://raw.githubusercontent.com/ZenWave360/spring-modulith-events-spring-cloud-stream/badges/branches.svg)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/actions/workflows/build.yml)
[![GitHub](https://img.shields.io/github/license/ZenWave360/spring-modulith-events-spring-cloud-stream)](https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/blob/main/LICENSE)

Spring-Modulith Events Externalizer that uses Spring Cloud Stream supporting both JSON and Avro serialization formats.

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

### Configuration
Enable Spring Cloud Stream event externalization by adding the `@EnableSpringCloudStreamEventExternalization` annotation to your Spring configuration:

```java
@Configuration
@EnableSpringCloudStreamEventExternalization
public class SpringCloudStreamEventsConfig {
    // Additional configurations (if needed)
}
```

This configuration ensures that all events of type `org.springframework.messaging.Message` with the header `SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_EVENT_HEADER` will be routed to their specified destination.

---

## Event Serialization

### JSON Serialization
Provides an `EventSerializer` that serializes `Message<?>` payloads into JSON format suitable for data storage adding a `_class` field to indicate the class type of the payload (needed for deserialization).

### Avro Serialization
If `com.fasterxml.jackson.dataformat.avro.AvroMapper` is present in the classpath, the serializer automatically supports Avro serialization/deserialization.

---

## Routing Events

### Programmatic Routing
You can define routing targets programmatically using Spring Messages:

```java
public class CustomerEventsProducer implements ICustomerEventsProducer {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void onCustomerCreated(CustomerCreated event) {
        Message<CustomerCreated> message = MessageBuilder.withPayload(event)
                .setHeader(SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_SENDTO_DESTINATION_HEADER, "customer-created")
                .build();
        applicationEventPublisher.publishEvent(message);
    }
}
```

### Annotation-Based Routing
Leverage the `@Externalized` annotation to define routing dynamically:

```java
@Externalized("customer-created::#{#this.getLastname()}")
class CustomerCreated {

    public String getLastname() {
        // Return the customer's last name
    }
}
```

Configure the routing in `application.yml`:

```yaml
spring:
  cloud:
    stream:
      bindings:
        customer-created:
          destination: customer-created
```

With this configuration, the `SpringCloudStreamEventExternalizer` dynamically sets the routing key (e.g., `kafka_messageKey` or `rabbit_routingKey`) based on the channel binder.

---

## Using Snapshot Versions
To include snapshot versions, add the following repository to your Maven configuration:

```xml
<repository>
    <id>gh</id>
    <url>https://raw.githubusercontent.com/ZenWave360/maven-snapshots/refs/heads/main</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
