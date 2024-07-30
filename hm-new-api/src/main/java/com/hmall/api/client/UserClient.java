package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/28 10:55
 */
@FeignClient("user-service")
public interface UserClient {
    @PutMapping("/money/deduct")
    void deductMoney(@RequestParam("pw")String pw,@RequestParam("amount")Integer amount);
}
