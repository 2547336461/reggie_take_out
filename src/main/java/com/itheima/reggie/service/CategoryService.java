package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;
import org.springframework.stereotype.Service;

/**
 * @Author Tao
 * @Date 2023 05 22 17 54
 **/

public interface CategoryService extends IService<Category> {
    //根据ID删除分类
    public void remove(Long id);
}
