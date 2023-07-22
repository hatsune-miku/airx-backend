package com.eggtartc.airxbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RedisKeys {
    UID_COUNTER("uid_counter", 1000);

    private final String key;
    private final Object def;
}
