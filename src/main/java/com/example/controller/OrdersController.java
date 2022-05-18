package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.entity.OrdersDto;
import com.example.service.OrderDetailService;
import com.example.service.OrdersService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;


    @PostMapping("/submit")
    public R<String> pay(@RequestBody Orders order){

        ordersService.submit(order);
        return R.success("支付成功!");
    }


    @GetMapping("/userPage")
    public R<Page> getPage(int page,int pageSize){
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        lambdaQueryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(ordersPage,lambdaQueryWrapper);

        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> list = ordersPage.getRecords();

        List<OrdersDto> dtoList = list.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            LambdaQueryWrapper<OrderDetail> lqw = new LambdaQueryWrapper<>();
            lqw.eq(OrderDetail::getOrderId,item.getId());

            ordersDto.setOrderDetails(orderDetailService.list(lqw));

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(dtoList);

        return R.success(ordersDtoPage);
    }

}
