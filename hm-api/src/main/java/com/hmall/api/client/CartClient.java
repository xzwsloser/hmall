package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Collections;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/27 23:23
 */
@FeignClient("cart-service")  // 表示需要拉取的服务
public interface CartClient {
    @DeleteMapping
    void deleteCartItemByIds(@RequestParam Collection<Long> ids);
}
