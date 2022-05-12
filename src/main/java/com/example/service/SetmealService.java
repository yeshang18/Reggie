package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Category;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDto;

public interface SetmealService extends IService<Setmeal> {

    public void saveSetmealDto(SetmealDto setmealDto);

    public SetmealDto getSetmealDto(Long id);

    public void updateSetmealDto(SetmealDto setmealDto);

}
