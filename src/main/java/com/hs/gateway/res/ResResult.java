package com.hs.gateway.res;

import java.io.Serializable;

/**
 * TODO
 *
 * @Author jinmu
 * @Date 2024/2/4 23:13
 */
public class ResResult<T> implements Serializable {

    private boolean success;
    private int code;
    private String message;
    private T data;
    private String detailMessage;

    public ResResult() {
    }

    public ResResult(T data) {
        this.success = true;
        this.code = ResResultCodeEnum.SUCCESS.getCode();
        this.message = ResResultCodeEnum.SUCCESS.getMsg();
        this.data = data;
    }

    public ResResult(String message, T data) {
        this.success = true;
        this.code = ResResultCodeEnum.SUCCESS.getCode();
        this.message = message;
        this.data = data;
    }

    public ResResult(int code, String message, T data) {
        this.success = this.success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ResResult(int code, String message, T data, String detailMessage) {
        this.success = this.success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.detailMessage = detailMessage;
    }

    public ResResult(ResResultCodeEnum resultCode, T data) {
        this.success = ResResultCodeEnum.SUCCESS.getCode() == resultCode.getCode();
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
        this.data = data;
    }

    public static <T> ResResult<T> success() {
        return new ResResult(null);
    }

    public static <T> ResResult<T> success(String message, T value) {
        return new ResResult(null);
    }

    public static <T> ResResult<T> success(T value) {
        return new ResResult(value);
    }

    public static <T> ResResult<T> error(String message) {
        return error(ResResultCodeEnum.FAIL, message);
    }

    public static <T> ResResult<T> error(String message, String exception) {
        return error(ResResultCodeEnum.FAIL, message, exception);
    }

    public static <T> ResResult<T> error(ResResultCodeEnum resultCode) {
        return new ResResult(resultCode, null);
    }

    public static <T> ResResult<T> error(ResResultCodeEnum resultCode, String customMsg) {
        return new ResResult(resultCode.getCode(), customMsg, null);
    }

    private static <T> ResResult<T> error(ResResultCodeEnum resultCode, String customMsg, String exception) {
        return new ResResult(resultCode.getCode(), customMsg, null, exception);
    }

    public static <T> ResResult<T> error(int code, String customMsg) {
        return new ResResult(code, customMsg, null);
    }

    public static <T> ResResult<T> error(int code, String customMsg, String exception) {
        return new ResResult(code, customMsg, null, exception);
    }

    public void setResultCode(ResResult<?> other) {
        if (other != null) {
            this.setCode(other.getCode());
            this.setMessage(other.getMessage());
            this.setSuccess(other.isSuccess());
        }

    }

    public void setResultCode(ResResultCodeEnum resultCode) {
        if (resultCode != null) {
            this.setCode(resultCode.getCode());
            this.setMessage(resultCode.getMsg());
            this.setSuccess(ResResultCodeEnum.SUCCESS == resultCode);
        }

    }

    public String toString() {
        return String.format("ResResult {code=%s,message=%s,data=%s}", this.code, this.message, this.data);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDetailMessage() {
        return this.detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }
}
