package com.hmall.api.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

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
}
