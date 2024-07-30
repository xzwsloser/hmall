package com.hmall.api.config;

import com.hmall.api.client.fallback.ItemClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/30 10:01
 */
@Configuration
public class FallbackConfig {
    @Bean
    public ItemClientFallback itemClientFallback(){
        return new ItemClientFallback();
    }
}
