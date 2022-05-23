package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Category;
import com.example.entity.Setmeal;
import com.example.entity.SetmealDish;
import com.example.entity.SetmealDto;
import com.example.mapper.CategoryMapper;
import com.example.mapper.SetmealMapper;
import com.example.service.CategoryService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.basic.BasicEditorPaneUI;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public void saveSetmealDto(SetmealDto setmealDto) {
        String key = "setmeal_"+setmealDto.getCategoryId()+"_"+setmealDto.getStatus();

        this.save(setmealDto);
        Long setmealId = setmealDto.getId();
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes.stream().map((item)->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());

        redisTemplate.delete(key);
        setmealDishService.saveBatch(dishes);
    }

    @Override
    public SetmealDto getSetmealDto(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto =new SetmealDto();

        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw =new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> dishes =setmealDishService.list(lqw);

        setmealDto.setSetmealDishes(dishes);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateSetmealDto(SetmealDto setmealDto) {

        String key = "setmeal_"+setmealDto.getCategoryId()+"_"+setmealDto.getStatus();
        this.updateById(setmealDto);
        LambdaQueryWrapper<SetmealDish> lqw =new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        redisTemplate.delete(key);
        setmealDishService.saveBatch(dishes);
    }

}
