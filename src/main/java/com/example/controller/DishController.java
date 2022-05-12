package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Category;
import com.example.entity.Dish;
import com.example.entity.DishDto;
import com.example.entity.DishFlavor;
import com.example.service.CategoryService;
import com.example.service.DishFlavorService;
import com.example.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> insert(@RequestBody DishDto dishDto){
        dishService.saveDishDto(dishDto);

        return R.success("添加成功!");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage =new Page<>();

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(name!=null,Dish::getName,name);
        lqw.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,lqw);

        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();


        List<DishDto> list= records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getOne(@PathVariable Long id){

        DishDto dishDto = dishService.getDishDtoById(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateDishDto(dishDto);
        return R.success("修改成功!");
    }

    @PostMapping("/status/0")
    public R<String> stop(Long[] ids){
        for (Long id:ids) {
            Dish dish = dishService.getById(id);
            if(dish!=null) {
                dish.setStatus(0);
                dishService.updateById(dish);
            }
        }
        return R.success("停售成功!");
    }

    @PostMapping("/status/1")
    public R<String> start(Long[] ids){
        for (Long id:ids) {
            Dish dish = dishService.getById(id);
            if(dish!=null) {
                dish.setStatus(1);
                dishService.updateById(dish);
            }
        }
        return R.success("启售成功!");
    }
    @DeleteMapping
    public R<String> delete(Long[] ids){
        for (Long id:ids) {
            dishService.removeById(id);
            LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
            lqw.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(lqw);
        }
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List> getList(Long categoryId,String name){
        LambdaQueryWrapper<Dish> lqw =new LambdaQueryWrapper<>();
        lqw.eq(categoryId!=null,Dish::getCategoryId,categoryId);
        lqw.like(name!=null,Dish::getName,name);
        lqw.orderByAsc(Dish::getSort);
        List list = dishService.list(lqw);

        return R.success(list);
    }

}
