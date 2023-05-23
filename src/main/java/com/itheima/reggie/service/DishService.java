package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

/**
 * @Author Tao
 * @Date 2023 05 22 19 40
 **/
public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜单对应的口味数据，需要操作两张表：dish,dish_flavor
    public void saveWithFlavor(DishDto dishDto);
}
