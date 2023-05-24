package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Tao
 * @Date 2023 05 24 13 36
 **/
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
