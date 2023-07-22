package com.eggtartc.airxbackend.form;

import lombok.Builder;
import lombok.Data;

public class MessageSend {
    @Data
    public static class Request {
        int type;
        String content;
    }

    @Data
    @Builder
    public static class Response {
        boolean success;
        String message;
    }
}
