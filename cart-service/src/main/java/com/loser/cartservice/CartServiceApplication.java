package com.loser.cartservice;

import com.hmall.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.hmall.api.client",defaultConfiguration = DefaultFeignConfig.class)  // 表示开启  OpenFeign,并且指定扫描包的位置
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
    // 注意启动类也是一个配置类,也可以进行相关的一些配置
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
