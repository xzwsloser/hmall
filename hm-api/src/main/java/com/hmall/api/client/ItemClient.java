package com.hmall.api.client;


import com.hmall.api.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/27 16:58
 */
@FeignClient("item-service")  // 请求的服务名称
public interface ItemClient {

    @GetMapping("/items")  // 请求的地址
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);  // 请求的参数和返回值,底层自动通过动态代理的方式获取到返回值
}
