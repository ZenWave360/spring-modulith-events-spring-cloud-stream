package io.zenwave360.modulith.events.scs;

import io.zenwave360.modulith.events.scs.config.EventSerializerConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = { TestsConfiguration.class })
@Import({ EventSerializerConfiguration.class })
@Transactional
public class SCSAvroEventExternalizerTest {

    @Autowired
    TestsConfiguration.CustomerEventsProducer customerEventsProducer;

    @MockitoSpyBean
    private StreamBridge streamBridge;

    @Test
    void testExternalizeAvroEvent() throws InterruptedException {
        var event = new io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent();
        event.setName("John Doe");

        customerEventsProducer.onCustomerEventAvro(event);

        // Wait for the event to be externalized
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(streamBridge).send(Mockito.eq("customers-avro-out-0"), Mockito.argThat(message -> {
                if (message instanceof Message<?>) {
                    var payload = ((Message<?>) message).getPayload();
                    return payload instanceof io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent
                            && "John Doe".equals(((io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent) payload).getName());
                }
                return false;
            }));
        });
    }

}
