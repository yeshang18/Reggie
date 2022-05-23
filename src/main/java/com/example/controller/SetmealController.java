package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.CustomException;
import com.example.common.R;
import com.example.entity.*;
import com.example.service.CategoryService;
import com.example.service.SetmealDishService;
import com.example.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @CacheEvict(value = "setmeal",key="#setmealDto.categoryId+'_'+#setmealDto.status")
    public R<String> insert(@RequestBody SetmealDto setmealDto){

        setmealService.saveSetmealDto(setmealDto);

        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> getPage(int page, int pageSize, String name){
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> pageDtoInfo = new Page<>();

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(name!=null,Setmeal::getName,name);
        lqw.orderByAsc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,lqw);

        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list= records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Long categoryId = setmealDto.getCategoryId();
            Category category = categoryService.getById(categoryId);
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(list);

        return R.success(pageDtoInfo);
    }


    @GetMapping("/{id}")
    public R<SetmealDto> getOne(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getSetmealDto(id);

        return R.success(setmealDto);
    }

    @PutMapping
    @CacheEvict(value = "setmeal",key="#setmealDto.categoryId+'_'+#setmealDto.status")
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateSetmealDto(setmealDto);

        return R.success("修改成功！");
    }

    @PostMapping("/status/0")
    @CacheEvict(value = "setmeal",allEntries = true)
    public R<String> stop(Long[] ids){
        for (Long id:ids) {
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal!=null) {
                setmeal.setStatus(0);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("停售成功!");
    }

    @PostMapping("/status/1")
    public R<String> start(Long[] ids){
        for (Long id:ids) {
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal!=null) {
                setmeal.setStatus(1);
                setmealService.updateById(setmeal);
            }
        }
        return R.success("启售成功!");
    }
    @DeleteMapping
    @CacheEvict(value = "setmeal",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){

        LambdaQueryWrapper<Setmeal> lqw1 =new LambdaQueryWrapper<>();
        lqw1.eq(Setmeal::getStatus,1);
        lqw1.in(Setmeal::getId,ids);
        int count = Math.toIntExact(setmealService.count(lqw1));
        if (count>0)
        {
            throw new  CustomException("套餐正在售卖中，不能删除!");
        }
        setmealService.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lqw);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public R<List<Setmeal>> getList(Setmeal setmeal){
        List<Setmeal> list;

//        String key = "setmeal_"+setmeal.getCategoryId()+"_"+setmeal.getStatus();
//
//        list = (List<Setmeal>) redisTemplate.opsForValue().get(key);

        /*if(list!=null)
            return R.success(list);*/
        LambdaQueryWrapper<Setmeal> lqw =new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,setmeal.getStatus());
        list = setmealService.list(lqw);

       // redisTemplate.opsForValue().set(key,list,60, TimeUnit.MINUTES);
        return R.success(list);
    }

    @GetMapping("/dish/{id}")
    public R<SetmealDto> getdish(@PathVariable String id){
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto =new SetmealDto();

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);

        List list = setmealDishService.list(lambdaQueryWrapper);

        BeanUtils.copyProperties(setmeal,setmealDto);

        String name = categoryService.getById(setmeal.getCategoryId()).getName();
        setmealDto.setCategoryName(name);
        setmealDto.setSetmealDishes(list);

        return R.success(setmealDto);

    }

}
