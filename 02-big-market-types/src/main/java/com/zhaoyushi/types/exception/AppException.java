package com.zhaoyushi.types.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;

    /**
     * 构造：仅携带异常码
     *
     * @param code 异常码
     */
    public AppException(String code) {
        this.code = code;
    }

    /**
     * 构造：携带异常码与原始异常
     *
     * @param code  异常码
     * @param cause 原始异常
     */
    public AppException(String code, Throwable cause) {
        this.code = code;
        super.initCause(cause);
    }

    /**
     * 构造：携带异常码与提示信息
     *
     * @param code    异常码
     * @param message 提示信息
     */
    public AppException(String code, String message) {
        this.code = code;
        this.info = message;
    }

    /**
     * 构造：携带异常码、提示信息与原始异常
     *
     * @param code    异常码
     * @param message 提示信息
     * @param cause   原始异常
     */
    public AppException(String code, String message, Throwable cause) {
        this.code = code;
        this.info = message;
        super.initCause(cause);
    }

    @Override
    public String toString() {
        return "com.zhaoyushi.x.api.types.exception.XApiException{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

}
