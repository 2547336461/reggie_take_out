package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author Tao
 * @Date 2023 05 23 14 03
 **/
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        // 清理redis所有菜品缓存
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);
        // 精确清理对应分类的缓存数据
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_"+categoryId+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功！");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        // 因为前端页面中有一个categoryName项，而pageInfo中没有，所以需要创建一个新的dishDtoPage对象包含categoryName字段方便返回
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        // 添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询
        dishService.page(pageInfo, queryWrapper);

        // 对象拷贝 将pageInfo中的属性 除了records之外全部拷贝到dishDtoPage
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        // 得到records对象单独处理
        List<Dish> records = pageInfo.getRecords();
        // 用Lambda表达式遍历每一个元素，并同时创建一个新的dishDto存储属性，并设置categoryName字段
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();// 拿到分类id
            // 根据id查到分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        // 清理redis所有菜品缓存
        // Set keys = redisTemplate.keys("dish_*");
        // redisTemplate.delete(keys);
        // 精确清理对应分类的缓存数据
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_"+categoryId+"_1";
        redisTemplate.delete(key);


        return R.success("修改菜品成功！");
    }

    /**
     * 根据ids更改菜品状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatusById(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("将要更改菜品状态的id个数为：{}", ids);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(ids != null, Dish::getId, ids);
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        for (Dish dish : list) {
            if (dish != null) {
                dish.setStatus(status);
                dishService.updateById(dish);
            }

        }
        return R.success("状态修改成功！");
    }

    /**
     * 根据ids删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteDishById(@RequestParam List<Long> ids) {
        // 查菜品的状态，是否可以删除（）
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.in(Dish::getId, ids);
        dishQueryWrapper.eq(Dish::getStatus, 1);

        int count = dishService.count(dishQueryWrapper);
        if (count > 0) {
            throw new CustomException("菜品正在售卖中！不能删除！");
        }

        // 删除dish
        dishService.removeByIds(ids);

        // 删除dish_flavor
        LambdaQueryWrapper<DishFlavor> dfQueryWrapper = new LambdaQueryWrapper<>();
        dfQueryWrapper.in(DishFlavor::getDishId, ids);

        dishFlavorService.remove(dfQueryWrapper);
        return R.success("删除菜品成功！");


    }

//    /**
//     * 根据条件查询对应的菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        // 构造查询条件
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
//        // 添加条件 查询状态为1的数据(起售)
//        queryWrapper.eq(Dish::getStatus,1);
//        // 排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }

    /**
     * 根据条件查询对应的菜品数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        // 动态拼接key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList != null) {
            // 如果存在，直接返回，无需查询
            return R.success(dishDtoList);
        }


        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        // 添加条件 查询状态为1的数据(起售)
        queryWrapper.eq(Dish::getStatus, 1);
        // 排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();// 拿到分类id
            // 根据id查到分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            // 当前菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            // SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，则查询数据库，并存放到redis缓存中
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
