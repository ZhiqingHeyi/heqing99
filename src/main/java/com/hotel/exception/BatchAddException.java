package com.hotel.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class BatchAddException extends RuntimeException {

    private final List<Map<String, String>> errors;

    public BatchAddException(List<Map<String, String>> errors) {
        super("批量添加房间时发生错误"); // 可以提供一个默认消息
        this.errors = errors;
    }

    public BatchAddException(String message, List<Map<String, String>> errors) {
        super(message);
        this.errors = errors;
    }
} 