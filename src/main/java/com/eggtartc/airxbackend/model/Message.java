package com.eggtartc.airxbackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class Message {
    private int senderUid;
    private Type type;
    private String rawContent;

    public String asText() {
        return rawContent;
    }

    public String asFileUrl() {
        return rawContent;
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        TEXT(1), FILE_URL(2);

        final int rawValue;

        public static Type fromRawValue(int rawValue) throws IllegalArgumentException {
            for (Type type : Type.values()) {
                if (type.rawValue == rawValue) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid rawValue: " + rawValue);
        }
    }
}
