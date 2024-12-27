package io.zenwave360.modulith.events.scs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ SpringCloudStreamEventExternalizerConfiguration.class, EventSerializerConfiguration.class})
public @interface EnableSpringCloudStreamEventExternalization {
}
