package io.zenwave360.modulith.events.scs.config;

import io.zenwave360.modulith.events.scs.SpringCloudStreamEventExternalizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.Externalized;
import org.springframework.modulith.events.RoutingTarget;
import org.springframework.modulith.events.config.EventExternalizationAutoConfiguration;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

@AutoConfiguration
@AutoConfigureAfter(EventExternalizationAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.modulith.events.externalization.enabled", havingValue = "true",
        matchIfMissing = true)
public class MessageExternalizationConfiguration {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
            .select(event -> EventExternalizationConfiguration.annotatedAsExternalized().test(event)
                    || event instanceof Message<?> && getTarget(event) != null)
            .route(Message.class, event -> RoutingTarget.forTarget(getTarget(event)).withoutKey())
            .build();
    }

    private String getTarget(Object event) {
        if (event instanceof Message<?> message) {
            return message.getHeaders()
                .get(SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_SENDTO_DESTINATION_HEADER, String.class);
        }
        return null;
    }
}
