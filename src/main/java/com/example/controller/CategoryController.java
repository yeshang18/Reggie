package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Category;
import com.example.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryservice;

    @PostMapping
    public R<String> insert(@RequestBody Category category){
        categoryservice.save(category);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        Page pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Category::getSort);

        categoryservice.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryservice.updateById(category);
        return R.success("修改成功!");
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        categoryservice.remove(ids);
        return R.success("删除成功!");
    }

    @GetMapping("/list")
    public R<List<Category>> getList(Category category){
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort);

        List<Category> list = categoryservice.list(lqw);

        return R.success(list);
    }

}
