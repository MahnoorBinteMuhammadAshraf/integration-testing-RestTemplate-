package com.advancedtaskmanager.exceptions;

import lombok.Data;

@Data
public class ErrorObject {
    String message;

    public ErrorObject(String message) {
        this.message = message;
    }
}
