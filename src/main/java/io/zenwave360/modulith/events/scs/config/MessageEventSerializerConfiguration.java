package io.zenwave360.modulith.events.scs.config;

import io.zenwave360.modulith.events.scs.AvroEventSerializer;
import io.zenwave360.modulith.events.scs.MessageEventSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.modulith.events.config.EventExternalizationAutoConfiguration;
import org.springframework.modulith.events.core.EventSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.avro.AvroMapper;

/**
 * Registers the {@link EventSerializer} used for the event publication log. Uses the application's
 * {@link JsonMapper} bean when present (e.g. from {@code spring-boot-starter-jackson}), otherwise falls
 * back to a default {@link JsonMapper}.
 */
@AutoConfiguration
@AutoConfigureAfter(EventExternalizationAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.modulith.events.externalization.enabled", havingValue = "true",
        matchIfMissing = true)
public class MessageEventSerializerConfiguration {

    @Bean
    @Primary
    @ConditionalOnClass(AvroMapper.class)
    public EventSerializer avroEventSerializer(ObjectProvider<JsonMapper> mapper) {
        return new AvroEventSerializer(mapper.getIfAvailable(JsonMapper::new));
    }

    @Bean
    @Primary
    @ConditionalOnMissingClass("tools.jackson.dataformat.avro.AvroMapper")
    public EventSerializer messageEventSerializer(ObjectProvider<JsonMapper> mapper) {
        return new MessageEventSerializer(mapper.getIfAvailable(JsonMapper::new));
    }

}
