package com.hmall.common.config;

import com.hmall.common.interceptor.UserInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/29 10:12
 */
@Configuration
@ConditionalOnClass(DispatcherServlet.class)   // 表示只要有 SpringMVC 都会
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new UserInterceptor());   // 都需要作用,反正不用拦截
    }
}
