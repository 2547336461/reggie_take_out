package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 *
 * @Author Tao
 * @Date 2023 05 22 20 09
 **/

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
