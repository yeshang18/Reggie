package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.DishDto;

public interface DishService extends IService<Dish> {
    public void saveDishDto(DishDto dishDto);

    public DishDto getDishDtoById(Long id);

    public void updateDishDto(DishDto dishDto);
}
