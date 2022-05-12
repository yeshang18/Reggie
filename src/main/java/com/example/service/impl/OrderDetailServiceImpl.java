package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.OrderDetail;
import com.example.entity.Setmeal;
import com.example.mapper.OrderDetailMapper;
import com.example.mapper.SetmealMapper;
import com.example.service.OrderDetailService;
import com.example.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
