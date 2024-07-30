package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/28 10:52
 */
@FeignClient("trade-service")
public interface OrderClient {
    @PutMapping("/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId")Long orderId);
}
