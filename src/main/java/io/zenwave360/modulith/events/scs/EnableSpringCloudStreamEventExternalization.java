package io.zenwave360.modulith.events.scs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SpringCloudStreamEventExternalizerConfiguration.class, MessageEventSerializerConfiguration.class, AvroEventSerializerConfiguration.class})
public @interface EnableSpringCloudStreamEventExternalization {
}
