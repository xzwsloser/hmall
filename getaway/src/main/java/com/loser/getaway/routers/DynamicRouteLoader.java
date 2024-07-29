package com.loser.getaway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/29 15:57
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouteLoader {

    private final NacosConfigManager nacosConfigManager;

    private final RouteDefinitionWriter routeDefinitionWriter;

    private final Set<String> router = new HashSet<>(); // 存储 id , 便于路由的删除

    private final String dataId = "gateway-routers.json";  // 使用 json 格式进行解析

    private final String group = "DEFAULT_GROUP";
    @PostConstruct  // 表示这一个类实例话之后执行
    public void initRouteConfigLoader() throws NacosException {
        // 1. 项目启动时,先拉取配置,之后添加配置监听器
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dataId, group, 5000, new Listener() {
            @Override
            public Executor getExecutor() {
                // 表示定义一个线程池,从线程池中取出线程执行这一个方法
                return null;
            }

            @Override
            public void receiveConfigInfo(String s) {
                // 2. 监听配置变更,需要更新路由
                updateConfig(s);
            }
        });
    }

    public void updateConfig(String configInfo){
        log.debug("开始更新路由信息");
        // 表示更新配置
        // 1. 解析配置文件,转化为 RouteDefination
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        // 删除旧的路由表
        // 开始删除旧的
        for (String r : router) {
            routeDefinitionWriter.delete(Mono.just(r)).subscribe();
        }
        // 清空路由表
        router.clear();
        for (RouteDefinition routeDefinition : routeDefinitions) {
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();  // 表示订阅容器中的消息
                // 记录路由 id
                router.add(routeDefinition.getId());
        }
    }
}
