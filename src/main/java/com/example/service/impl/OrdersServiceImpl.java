package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.BaseContext;
import com.example.common.CustomException;
import com.example.entity.*;
import com.example.mapper.OrdersMapper;
import com.example.mapper.SetmealMapper;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;


    @Override
    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,userId);

        List<ShoppingCart> scList = shoppingCartService.list(lqw);

        if(scList==null||scList.size()==0){
            throw new CustomException("购物车为空，不能支付！");
        }

        User user = userService.getById(userId);

        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null)
        {
            throw new CustomException("地址为空!");
        }

        AtomicInteger amount = new AtomicInteger(0);

        Long orderId = IdWorker.getId();
        List<OrderDetail> orderList = scList.stream().map((item)->{
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                orderDetail.setAmount(item.getAmount());
                orderDetail.setNumber(item.getNumber());
                orderDetail.setDishFlavor(item.getDishFlavor());
                orderDetail.setDishId(item.getDishId());
                orderDetail.setImage(item.getImage());
                orderDetail.setSetmealId(item.getSetmealId());
                orderDetail.setName(item.getName());

                amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
                return orderDetail;

        }).collect(Collectors.toList());

        orderDetailService.saveBatch(orderList);

        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()!=null?"":addressBook.getProvinceName())
                        +(addressBook.getCityName()!=null?"":addressBook.getCityName())
                        +(addressBook.getDistrictName()!=null?"":addressBook.getDistrictName())
                        +(addressBook.getDetail()!=null?"":addressBook.getDetail())
                        );
        this.save(orders);

        shoppingCartService.remove(lqw);

    }


}
