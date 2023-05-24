package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Author Tao
 * @Date 2023 05 22 19 41
 **/
public interface SetmealService extends IService<Setmeal> {
    // 新增套餐，同时需要保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    // 删除套餐，同时需要删除套餐和菜品的关联关系
    public void deleteWithDish(List<Long> ids);
}
