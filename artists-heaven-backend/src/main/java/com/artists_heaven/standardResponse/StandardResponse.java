package com.artists_heaven.standardResponse;

import lombok.Getter;

@Getter
public class StandardResponse<T> {
    private String message;
    private T data;
    private int status;

    public StandardResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public StandardResponse(String message, int status) {
        this(message, null, status);
    }
}
