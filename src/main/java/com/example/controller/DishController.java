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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @CacheEvict(value = "dishCache",key="#dishDto.categoryId+'_'+#dishDto.status")
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
    @CacheEvict(value = "dishCache",key="#dishDto.categoryId+'_'+#dishDto.status")
    public R<String> update(@RequestBody DishDto dishDto){

        dishService.updateDishDto(dishDto);
        return R.success("修改成功!");
    }

    @PostMapping("/status/0")
    @CacheEvict(value = "dishCache",allEntries = true)
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

   /* @GetMapping("/list")
    public R<List<DishDto>> getList(Dish dish){
        LambdaQueryWrapper<Dish> lqw =new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId,dish.getCategoryId());
        lqw.eq(Dish::getStatus,1);
        lqw.like(dish.getName()!=null,Dish::getName,dish.getName());
        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(lqw);

        List<DishDto> dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();

            Category category =categoryService.getById(categoryId);
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }

            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            dishDto.setFlavors(dishFlavorService.list(lambdaQueryWrapper));

            return dishDto;
        }).collect(Collectors.toList());
            return R.success(dishDtoList);
    }
    */


        @GetMapping("/list")

        @Cacheable(value = "dishCache",key = "#dish.categoryId+'_'+#dish.status")
        public R<List<DishDto>> list(Dish dish){
            List<DishDto> dishDtoList;

            /*String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();

            dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

            if(dishDtoList!=null)
                return R.success(dishDtoList);*/
            //构造查询条件
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
            //添加条件，查询状态为1（起售状态）的菜品
            queryWrapper.eq(Dish::getStatus,1);

            //添加排序条件
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

            List<Dish> list = dishService.list(queryWrapper);

            dishDtoList = list.stream().map((item) -> {
                DishDto dishDto = new DishDto();

                BeanUtils.copyProperties(item,dishDto);

                Long categoryId = item.getCategoryId();//分类id
                //根据id查询分类对象
                Category category = categoryService.getById(categoryId);

                if(category != null){
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
                }

                //当前菜品的id
                Long dishId = item.getId();
                LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
                //SQL:select * from dish_flavor where dish_id = ?
                List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
                dishDto.setFlavors(dishFlavorList);
                return dishDto;
            }).collect(Collectors.toList());

//            redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
            return R.success(dishDtoList);
        }



}
