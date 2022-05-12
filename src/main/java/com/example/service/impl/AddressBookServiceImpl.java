package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.AddressBook;
import com.example.entity.Setmeal;
import com.example.mapper.AddressBookMapper;
import com.example.mapper.SetmealMapper;
import com.example.service.AddressBookService;
import com.example.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
