package io.zenwave360.modulith.events.scs;

import io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.events.core.EventSerializer;
import tools.jackson.databind.json.JsonMapper;

public class AvroEventSerializerTest {

    private EventSerializer eventSerializer;

    @BeforeEach
    public void setUp() {
        eventSerializer = new AvroEventSerializer(new JsonMapper());
    }

    @Test
    public void testSerializeMessage() {
        var customerEvent = new CustomerEvent();
        customerEvent.setName("John Doe");
        Message<?> message = MessageBuilder.withPayload(customerEvent).setHeader("headerKey", "headerValue").build();

        Object serialized = eventSerializer.serialize(message);

        Assertions.assertTrue(serialized instanceof String);
        Assertions.assertTrue(serialized.toString().contains("\"name\":\"John Doe\","));
        Assertions.assertTrue(serialized.toString()
            .contains("\"_class\":\"io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent\""));
    }

    @Test
    public void testDeserializeObject() {
        String serializedObject = """
                  {
                    "name" : "John Doe"
                  }
                """;
        CustomerEvent deserialized = eventSerializer.deserialize(serializedObject, CustomerEvent.class);

        Assertions.assertEquals("John Doe", deserialized.getName());
    }

    @Test
    public void testDeserializeMessage() {
        String serializedMessage = """
                {
                  "headers" : {
                    "headerKey" : "headerValue",
                    "id" : "cc76cfb5-4ba3-5bf1-f652-9f44dfc24c85",
                    "timestamp" : 1735318992520
                  },
                  "payload" : {
                    "name" : "John Doe",
                    "addresses" : [ ],
                    "paymentMethods" : [ ],
                    "_class" : "io.zenwave360.modulith.events.scs.dtos.avro.CustomerEvent"
                  }
                }
                """;
        Message<?> deserialized = eventSerializer.deserialize(serializedMessage, Message.class);

        Assertions.assertTrue(deserialized.getPayload() instanceof CustomerEvent);
    }

    @Test
    public void testSerializeDeserializeMessage() {
        var customerEvent = new CustomerEvent();
        customerEvent.setName("John Doe");
        Message<?> message = MessageBuilder.withPayload(customerEvent).setHeader("headerKey", "headerValue").build();

        Object serialized = eventSerializer.serialize(message);
        Message<?> deserialized = eventSerializer.deserialize(serialized, Message.class);

        Assertions.assertEquals(message.getPayload().getClass(), deserialized.getPayload().getClass());
    }

    @Test
    public void testSerializeDeserializeObject() {
        var customerEvent = new CustomerEvent();
        customerEvent.setName("John Doe");

        Object serialized = eventSerializer.serialize(customerEvent);
        Object deserialized = eventSerializer.deserialize(serialized, CustomerEvent.class);

        Assertions.assertEquals(CustomerEvent.class, deserialized.getClass());
        Assertions.assertEquals(customerEvent.getName(), ((CustomerEvent) deserialized).getName());
    }

}
