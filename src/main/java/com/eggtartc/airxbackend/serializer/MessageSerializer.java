package com.eggtartc.airxbackend.serializer;

import com.eggtartc.airxbackend.model.Message;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

// 4 Bytes: senderUid
// 4 Bytes: type
// 4 Bytes: rawContentLength
// N Bytes: rawContent
public class MessageSerializer implements Serializer<Message> {
    @Override
    public byte[] serialize(String topic, Message data) {
        // String and four integers
        byte[] rawContent = data.getRawContent().getBytes(StandardCharsets.UTF_8);
        int length = rawContent.length + 4 * 3;
        byte[] bytes = new byte[length];

        System.arraycopy(bytesFromInteger(data.getSenderUid()), 0, bytes, 0, 4);
        System.arraycopy(bytesFromInteger(data.getType().getRawValue()), 0, bytes, 4, 4);
        System.arraycopy(bytesFromInteger(rawContent.length), 0, bytes, 8, 4);
        System.arraycopy(rawContent, 0, bytes, 12, rawContent.length);

        return bytes;
    }

    private byte[] bytesFromInteger(int v) {
        return new byte[] {
            (byte) (v >>> 24),
            (byte) (v >>> 16),
            (byte) (v >>> 8),
            (byte) v
        };
    }

}
