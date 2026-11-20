## What's Changed

Release notes for the **1.4.0** release.

## What's New

### Spring Modulith 2.2 and Spring Boot 4.2

The library is now built and tested against Spring Modulith 2.2.0 and Spring Boot 4.2.0 (up from
Spring Modulith 2.1.2 and Spring Boot 4.1.2). Spring Cloud stays on 2025.1.0.

1.4.x requires Spring Modulith 2.2.x. Applications that stay on Spring Modulith 2.1.x / Spring
Boot 4.1.x should keep using the 1.3.x line, which is still maintained on the `maintenance/1.3.x`
branch.

### Event Externalizer API Migration

Spring Modulith 2.2 removed `org.springframework.modulith.events.support.DelegatingEventExternalizer`.
`SpringCloudStreamEventExternalizerConfiguration` now registers Spring Cloud Stream as an
`EventExternalizationTransport` of Spring Modulith's `EventExternalizerModuleListener`, the same
way Spring Modulith's own broker integrations (Kafka, AMQP, ...) do.

This change is internal. Configuration properties, `@Externalized` routing, message keys and
headers, and JSON/Avro serialization work as before.

## Upgrade Notes

### Incomplete Event Publications

Spring Modulith stores the id of the target listener with every event publication. Because the
listener class changed, the id changed too:

| Version | Listener id                                                                                            |
|---------|--------------------------------------------------------------------------------------------------------|
| 1.3.x   | `org.springframework.modulith.events.support.DelegatingEventExternalizer.externalize(java.lang.Object)`     |
| 1.4.x   | `org.springframework.modulith.events.support.EventExternalizerModuleListener.externalize(java.lang.Object)` |

Incomplete publications written by 1.3.x keep the old id. After the upgrade there is no listener
with that id, so they are not resubmitted. Before upgrading, either:

- let all outstanding publications complete (no incomplete rows left in `event_publication`), or
- move them to the new id after the upgrade:

```sql
UPDATE event_publication
   SET listener_id = 'org.springframework.modulith.events.support.EventExternalizerModuleListener.externalize(java.lang.Object)'
 WHERE listener_id = 'org.springframework.modulith.events.support.DelegatingEventExternalizer.externalize(java.lang.Object)'
   AND completion_date IS NULL;
```

The format of the serialized event is unchanged, so publications moved this way are deserialized
and published normally.

## Internal

- CI: the dependency update workflow now keeps `main` on the latest Spring Modulith 2.x and
  Spring Boot 4.x, and keeps `maintenance/1.3.x` on Spring Modulith 2.1.x and Spring Boot 4.1.x.
- Documentation: the README versions table lists 1.4.x.

**Full Changelog**: https://github.com/ZenWave360/spring-modulith-events-spring-cloud-stream/compare/v1.3.1...v1.4.0
