package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Orders;
import com.example.entity.Setmeal;
import com.example.mapper.OrdersMapper;
import com.example.mapper.SetmealMapper;
import com.example.service.OrdersService;
import com.example.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
}
