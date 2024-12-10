package io.zenwave360.modulith.events.scs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AvroEventSerializerConfiguration.class, MessageEventSerializerConfiguration.class, SpringCloudStreamEventExternalizerConfiguration.class})
public @interface EnableSpringCloudStreamEventExternalization {
}
