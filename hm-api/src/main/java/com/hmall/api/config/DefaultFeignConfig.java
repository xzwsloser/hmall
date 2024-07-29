package com.hmall.api.config;

import com.hmall.api.client.UserClient;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

import java.time.temporal.Temporal;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/27 18:12
 */
public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;  // 表示记录所有的注解
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = UserContext.getUser();
                if(userId != null){
                    requestTemplate.header("user-info", userId.toString());
                }
            }
        };
    }
}
