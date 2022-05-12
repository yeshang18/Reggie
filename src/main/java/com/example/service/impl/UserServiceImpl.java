package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Setmeal;
import com.example.entity.User;
import com.example.mapper.SetmealMapper;
import com.example.mapper.UserMapper;
import com.example.service.SetmealService;
import com.example.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
