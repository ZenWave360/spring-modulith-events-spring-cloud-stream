package io.zenwave360.modulith.events.scs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zenwave360.modulith.events.scs.config.SpringCloudStreamEventExternalizerConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@EnableAutoConfiguration
@Import({ SpringCloudStreamEventExternalizerConfiguration.class })
@EmbeddedKafka(partitions = 1, topics = { "customers-json-topic" })
@EnableTransactionManagement
public class TestsConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper(); // Customize if needed
    }

    @Bean
    EmbeddedKafkaBroker embeddedKafkaBroker() {
        return new EmbeddedKafkaZKBroker(1, true, 1, "customers-json-topic");
    }

    @Bean
    CustomerEventsProducer customerEventsProducer(ApplicationEventPublisher applicationEventPublisher) {
        return new CustomerEventsProducer(applicationEventPublisher);
    }

    static class CustomerEventsProducer {

        private final ApplicationEventPublisher applicationEventPublisher;

        public CustomerEventsProducer(ApplicationEventPublisher applicationEventPublisher) {
            this.applicationEventPublisher = applicationEventPublisher;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void onCustomerEventJson(io.zenwave360.modulith.events.scs.dtos.json.CustomerEvent event) {
            Message<io.zenwave360.modulith.events.scs.dtos.json.CustomerEvent> message = MessageBuilder.withPayload(event)
                    .setHeader(
                            SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_SENDTO_DESTINATION_HEADER,
                            "customers-json-out-0") // <- target binding name
                    .build();
            applicationEventPublisher.publishEvent(message);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void onCustomerEventAvro(io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent event) {
            Message<io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent> message = MessageBuilder.withPayload(event)
                    .setHeader(
                            SpringCloudStreamEventExternalizer.SPRING_CLOUD_STREAM_SENDTO_DESTINATION_HEADER,
                            "customers-avro-out-0") // <- target binding name
                    .build();
            applicationEventPublisher.publishEvent(message);
        }
    }
}
