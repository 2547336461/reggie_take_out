package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @Author Tao
 * @Date 2023 05 17 18 45
 **/
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 自定义SQL语句异常处理办法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({SQLIntegrityConstraintViolationException.class})
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 自定义菜单和菜品删除异常处理办法
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({CustomException.class})
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
