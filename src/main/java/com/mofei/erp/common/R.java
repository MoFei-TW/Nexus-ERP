package com.mofei.erp.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public record R<T>(
    int code,
    String message,
    T data,
    LocalDateTime timestamp
) implements Serializable {

    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null, LocalDateTime.now());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data, LocalDateTime.now());
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data, LocalDateTime.now());
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null, LocalDateTime.now());
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, LocalDateTime.now());
    }
}
