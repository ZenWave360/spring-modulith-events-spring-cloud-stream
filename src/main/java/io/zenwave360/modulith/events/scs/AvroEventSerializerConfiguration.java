package io.zenwave360.modulith.events.scs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.events.config.EventExternalizationAutoConfiguration;
import org.springframework.modulith.events.core.EventSerializer;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@AutoConfigureAfter(EventExternalizationAutoConfiguration.class)
@ConditionalOnClass(AvroMapper.class)
@ConditionalOnProperty(name = "spring.modulith.events.externalization.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class AvroEventSerializerConfiguration {

    @Bean
    @Primary
    public EventSerializer customEventSerializer(EventSerializer defaultEventSerializer, ObjectMapper mapper) {
        AvroMapper avroMapper = AvroMapper.builder().build();
        return new EventSerializer() {

            @Override
            public @NotNull Object serialize(Object event) {
                if (event instanceof Message<?> message) {
                    Map<String, Object> serializedMessage = new HashMap<>();
                    serializedMessage.put("headers", message.getHeaders());
                    Object payload = serializePayload(message.getPayload());
                    serializedMessage.put("payload", payload);

                    return defaultEventSerializer.serialize(serializedMessage);
                }
                return serializePayload(event);
            }

            private Object serializePayload(Object payload) {
                if(payload instanceof SpecificRecord || payload instanceof GenericRecord) {
                    ObjectNode objectNode = avroMapper.valueToTree(payload);
                    objectNode.put("_class", payload.getClass().getName());
                    objectNode.remove("specificData"); // TODO: remove this recursively
                    return mapper.convertValue(objectNode, Map.class);
                }
                return defaultEventSerializer.serialize(payload);
            }

            @Override
            public <T> @NotNull T deserialize(Object serialized, Class<T> type) {
                try {
                    return unsafeDeserialize(serialized, type);
                } catch (JsonProcessingException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            private <T> @NotNull T unsafeDeserialize(Object serialized, Class<T> type) throws JsonProcessingException, ClassNotFoundException {
                if(Message.class.isAssignableFrom(type)) {
                    JsonNode node = mapper.readTree(serialized.toString());
                    JsonNode headersNode = node.get("headers");
                    Map<String, Object> headers = mapper.convertValue(headersNode, Map.class);
                    JsonNode payloadNode = node.get("payload");
                    Object payload = null;
                    if (payloadNode.get("_class") != null) {
                        Class<?> payloadType = Class.forName(payloadNode.get("_class").asText());
                        payload = mapper.treeToValue(payloadNode, payloadType);
                    } else {
                        payload = mapper.treeToValue(payloadNode, Object.class);
                    }
                    return (T) MessageBuilder.createMessage(payload, new MessageHeaders(headers));
                }
                return defaultEventSerializer.deserialize(serialized, type);
            }
        };
    }
}
