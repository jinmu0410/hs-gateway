package com.hs.gateway.res;

public enum ResResultCodeEnum {
    SUCCESS(200, "Request succeeded."),
    FAIL(500, "Processing failed.");

    private final int code;
    private final String msg;

    ResResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
