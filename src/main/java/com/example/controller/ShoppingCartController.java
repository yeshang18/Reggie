package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.ShoppingCart;
import com.example.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List> getList(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        List list = shoppingCartService.list(lambdaQueryWrapper);
        return R.success(list);
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody ShoppingCart shoppingCart){
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(shoppingCart.getDishId() !=null,ShoppingCart::getDishId,shoppingCart.getDishId());
        lqw.eq(shoppingCart.getSetmealId() !=null,ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        ShoppingCart cart = shoppingCartService.getOne(lqw);
        if(cart!=null){
            cart.setNumber(cart.getNumber()+1);
            cart.setCreateTime(LocalDateTime.now());
           // cart.setAmount(cart.getAmount().add(shoppingCart.getAmount()));
            shoppingCartService.updateById(cart);
            return R.success("添加成功!");
        }

        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartService.save(shoppingCart);
        return R.success("添加成功！");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart cart){

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(cart.getDishId()!=null,ShoppingCart::getDishId,cart.getDishId());
        lambdaQueryWrapper.eq(cart.getSetmealId()!=null,ShoppingCart::getSetmealId,cart.getSetmealId());
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        ShoppingCart shoppingCart =shoppingCartService.getOne(lambdaQueryWrapper);
        if(shoppingCart.getNumber()>1){
            shoppingCart.setNumber(shoppingCart.getNumber()-1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(shoppingCart);
        }
        else if(shoppingCart.getNumber()==1){
            shoppingCartService.remove(lambdaQueryWrapper);
        }
        return R.success("修改成功!");
    }

    @DeleteMapping("/clean")
    public R<String> delete(){
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(lambdaQueryWrapper);
        return R.success("删除成功!");
    }


}
