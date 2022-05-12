package com.example.filter;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 */

@Slf4j
@WebFilter(filterName = "LocalCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //获得本次请求URI

        String requestURI = request.getRequestURI();
        //判断是否处理
        String[] uris = new String[]{"/employee/login","/employee.logout","/backend/**","/front/**","/common/**"};
        boolean check = check(uris, requestURI);
        //不需要则直接放行
        if(check){
            filterChain.doFilter(request,response);
            return;
        }
        //判断登录状态
        if(request.getSession().getAttribute("employee")!=null)
        {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request,response);
            return;
        }
        //未登录则跳转登陆页面，通过输出流向客户端响应
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }


    public boolean check(String[] urls,String uri){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url, uri);
            if(match)return true;
        }
        return false;
    }
}
