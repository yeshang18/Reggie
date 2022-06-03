package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.R;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.descriptor.web.SecurityRoleRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){


        String phone = user.getPhone();
        if(phone !=null)
        {
            //String code = ValidateCodeUtils.generateValidateCode(4).toString();

            String code = "1234";
            session.setAttribute(phone,code);  //存入session中

            //存入Redis中，并设置5分钟

           // stringRedisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("发送成功");
        }

        return R.error("发送失败！");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){

        String phone =map.get("phone").toString();
        String code = map.get("code").toString();
        //session获取验证码
        Object codeInsSession =session.getAttribute(phone);
        if(codeInsSession.equals(code))
        {
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);

            User user = userService.getOne(lqw);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        //redis获取验证码
     /*   String codeInsSession = stringRedisTemplate.opsForValue().get(phone);
        if (codeInsSession!=null && codeInsSession.equals(code)) {
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone, phone);

            User user = userService.getOne(lqw);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //用户登陆成功，删除验证码
            stringRedisTemplate.delete(phone);
            return R.success(user);
        }*/
        return R.error("登陆失败，请检查验证码是否正确!");

    }

}
