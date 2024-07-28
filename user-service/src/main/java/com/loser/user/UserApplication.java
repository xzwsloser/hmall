package com.loser.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/27 21:14
 */
@SpringBootApplication
@MapperScan("com.loser.user.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
