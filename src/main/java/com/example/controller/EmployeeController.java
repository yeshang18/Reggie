package com.example.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.R;
import com.example.entity.Employee;
import com.example.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        //将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //根据用户名username查询数据库
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(lqw);

        //判断是否存在用户，无则返回错误
        if(emp == null){
            return R.error("登陆失败!");
        }

        //判断密码是否正确
        if (!emp.getPassword().equals(password)){
            return R.error("登录错误!");
        }

        //判断是否被禁用
        if(emp.getStatus()!=1){
            return R.error("用户被禁用!");
        }

        //返回成功
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除Session中保存的当前员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 添加员工
     * @param employee
     * @param servletRequest
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody Employee employee,HttpServletRequest servletRequest){
       // log.info("请求成功"+employee.toString());
//        Long id = Thread.currentThread().getId();
//        log.info("线程"+id);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser((Long) servletRequest.getSession().getAttribute("employee"));
//        employee.setUpdateUser((Long) servletRequest.getSession().getAttribute("employee"));

        employeeService.save(employee);
        return R.success("添加成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        lqw.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> disabled(@RequestBody Employee employee){
//        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
//        employee.setUpdateTime(LocalDateTime.now());
      employeeService.updateById(employee);
        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> update(@PathVariable String id){
        Employee emp = employeeService.getById(id);
        if(emp!=null)
            return R.success(emp);
        else
            return R.error("查询失败！");
    }

}
