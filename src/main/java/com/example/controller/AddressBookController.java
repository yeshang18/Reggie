package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.BaseContext;
import com.example.common.R;
import com.example.entity.AddressBook;
import com.example.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;


    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success("保存成功！");
    }

    @GetMapping("/list")
    public R<List<AddressBook>> getList(){
        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());

        List list = addressBookService.list(lambdaQueryWrapper);

        return R.success(list);
    }

    @PutMapping("/default")
    @Transactional
    public R<String> setDefault(@RequestBody AddressBook addressBook){

        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        lambdaQueryWrapper.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook1 = addressBookService.getOne(lambdaQueryWrapper);
        if(addressBook1!=null){
            addressBook1.setIsDefault(0);
            addressBookService.updateById(addressBook1);
        }

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success("修改成功！");

    }

    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        lqw.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook = addressBookService.getOne(lqw);
        return R.success(addressBook);
    }
}
