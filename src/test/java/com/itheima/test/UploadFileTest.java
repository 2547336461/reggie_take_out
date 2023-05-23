package com.itheima.test;

import org.junit.jupiter.api.Test;

/**
 * @Author Tao
 * @Date 2023 05 23 09 14
 **/

public class UploadFileTest {
    @Test
    public void test1(){
        String fileName = "erers.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
