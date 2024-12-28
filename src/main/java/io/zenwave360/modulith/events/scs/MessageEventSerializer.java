package io.zenwave360.modulith.events.scs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.modulith.events.core.EventSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MessageEventSerializer implements EventSerializer {

    private final ObjectMapper jacksonMapper;

    public MessageEventSerializer(ObjectMapper jacksonMapper) {
        this.jacksonMapper = jacksonMapper;
    }

    protected Map<String, Object> serializeToMap(Object payload) {
        ObjectNode objectNode = jacksonMapper.valueToTree(payload);
        return jacksonMapper.convertValue(objectNode, Map.class);
    }

    @Override
    public Object serialize(Object event) {
        if (event instanceof Message<?> message) {
            Map<String, Object> serializedMessage = new HashMap<>();
            serializedMessage.put("headers", message.getHeaders());

            var payload = serializeToMap(message.getPayload());
            payload.put("_class", message.getPayload().getClass().getName());
            serializedMessage.put("payload", payload);

            return jacksonSerialize(serializedMessage);
        }
        return jacksonSerialize(event);
    }

    @Override
    public <T> T deserialize(Object serialized, Class<T> type) {
        try {
            return unsafeDeserialize(serialized, type);
        }
        catch (JsonProcessingException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T unsafeDeserialize(Object serialized, Class<T> type)
            throws JsonProcessingException, ClassNotFoundException {
        if (Message.class.isAssignableFrom(type)) {
            JsonNode node = jacksonMapper.readTree(serialized.toString());
            JsonNode headersNode = node.get("headers");
            Map<String, Object> headers = jacksonMapper.convertValue(headersNode, Map.class);
            JsonNode payloadNode = node.get("payload");
            Object payload = null;
            if (payloadNode.get("_class") != null) {
                Class<?> payloadType = Class.forName(payloadNode.get("_class").asText());
                if (payloadNode instanceof ObjectNode objectNode) {
                    objectNode.remove("_class");
                }
                payload = deserializePayload(payloadNode, payloadType);
            }
            else {
                payload = deserializePayload(payloadNode, Object.class);
            }
            return (T) MessageBuilder.createMessage(payload, new MessageHeaders(headers));
        }
        return jacksonDeserialize(serialized, type);
    }

    protected <T> T deserializePayload(TreeNode payloadNode, Class<T> payloadType) throws JsonProcessingException {
        return jacksonMapper.treeToValue(payloadNode, payloadType);
    }

    protected Object jacksonSerialize(Object event) {
        try {
            var map = serializeToMap(event);
            return jacksonMapper.writeValueAsString(map);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T jacksonDeserialize(Object serialized, Class<T> type) {
        try {
            JsonNode node = jacksonMapper.readTree(serialized.toString());
            return (T) jacksonMapper.readerFor(type).readValue(node);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
