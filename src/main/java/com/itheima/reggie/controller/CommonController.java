package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 *
 * @Author Tao
 * @Date 2023 05 23 08 45
 **/
@RestController
@RequestMapping("common")
@Slf4j
public class CommonController {

    // 导入配置文件中的存储路径
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file是一个临时文件，需要立即转存到指定位置来保存，否则本次请求结束后临时文件会删除
        log.info(file.toString());
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件重名被覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        // 防止目录不存在，加个判断
        File dir = new File(basePath);
        if (!dir.exists()){
            // 目录不存在，需要创建
            dir.mkdirs();
        }

        try {
            //将临时文件转存到指定位置(通过配置文件更改位置)
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }



}
