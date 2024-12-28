package io.zenwave360.modulith.events.scs;

import io.zenwave360.modulith.events.scs.config.EventSerializerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = { TestsConfiguration.class })
@Import({ EventSerializerConfiguration.class })
@Transactional
public class SCSJsonEventExternalizerTest {

    @Autowired
    TestsConfiguration.CustomerEventsProducer customerEventsProducer;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void testExternalizeJsonEvent() throws InterruptedException {
        var event = new io.zenwave360.modulith.events.scs.dtos.json.CustomerEvent().withName("John Doe");
        customerEventsProducer.onCustomerEventJson(event);
        // Wait for the event to be externalized
        Thread.sleep(5000);
        // TODO: Assert that the event was externalized
    }

}
