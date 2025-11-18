package com.example.fxdeals.core;

public class ErrorRow {
    private final int index;
    private final String code;
    private final String message;

    public ErrorRow(int index, String code, String message) {
        this.index = index;
        this.code = code;
        this.message = message;
    }
    public int getIndex() { return index; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
}
