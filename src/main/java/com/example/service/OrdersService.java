package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Orders;
import com.example.entity.Setmeal;

public interface OrdersService extends IService<Orders> {


    public void submit(Orders orders);
}
