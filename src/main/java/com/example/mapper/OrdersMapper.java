package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author ${author}
 * @since 2022-05-10
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}
