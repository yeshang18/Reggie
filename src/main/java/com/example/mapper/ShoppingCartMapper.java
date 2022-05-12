package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 * 购物车 Mapper 接口
 * </p>
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

}
