package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.CustomException;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.Setmeal;
import com.example.mapper.CategoryMapper;
import com.example.service.CategoryService;
import com.example.service.DishService;
import com.example.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper();
        lqw.eq(Dish::getCategoryId,id);
        //查询当前分类是否关联菜品，若关联，抛出业务异常
        long count = dishService.count(lqw);
        if(count>0){
            throw new CustomException("当前分类关联了菜品，不能删除!");
        }

        //查询当前分类是否关联菜品，若关联，抛出业务异常

        LambdaQueryWrapper<Setmeal> lqw1 = new LambdaQueryWrapper();
        lqw1.eq(Setmeal::getCategoryId,id);
        long count1 = setmealService.count(lqw1);
        if(count1>0){
            throw new CustomException("当前分类关联了菜单，不能删除!");
        }

        super.removeById(id);
    }
}
