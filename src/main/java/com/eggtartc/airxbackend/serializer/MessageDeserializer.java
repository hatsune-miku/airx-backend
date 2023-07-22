package com.eggtartc.airxbackend.serializer;

import com.eggtartc.airxbackend.model.Message;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

// 4 Bytes: senderUid
// 4 Bytes: type
// 4 Bytes: rawContentLength
// N Bytes: rawContent
public class MessageDeserializer implements Deserializer<Message> {
    @Override
    public Message deserialize(String topic, byte[] data) {
        try {
            int senderUid = integerFromBytes(bytesOffset(data, 0));
            int typeRawValue = integerFromBytes(bytesOffset(data, 4));
            int rawContentLength = integerFromBytes(bytesOffset(data, 8));

            String rawContent = new String(bytesOffset(data, 12), 0, rawContentLength, StandardCharsets.UTF_8);
            Message.Type type = Message.Type.fromRawValue(typeRawValue);

            return Message.builder()
                .senderUid(senderUid)
                .type(type)
                .rawContent(rawContent)
                .build();
        } catch (Exception e) {
            // Null is a valid value for Kafka
            return null;
        }
    }

    public int integerFromBytes(byte[] bytes) {
        return ((bytes[0] & 0xff) << 24)
            | ((bytes[1] & 0xff) << 16)
            | ((bytes[2] & 0xff) << 8)
            | (bytes[3] & 0xff);
    }

    public byte[] bytesOffset(byte[] bytes, int offset) {
        byte[] result = new byte[bytes.length - offset];
        System.arraycopy(bytes, offset, result, 0, result.length);
        return result;
    }
}
