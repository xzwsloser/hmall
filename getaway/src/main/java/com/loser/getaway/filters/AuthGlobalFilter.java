package com.loser.getaway.filters;
import com.hmall.common.exception.UnauthorizedException;
import com.loser.getaway.config.AuthProperties;
import com.loser.getaway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.WebClientHttpRoutingFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author xzw
 * @version 1.0
 * @Description
 * @Date 2024/7/28 22:32
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(); // 用于匹配 ant 风格的路径
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 判断 request
        ServerHttpRequest request = exchange.getRequest();
        // 2. 判断是否需要登录校验
        if(isExclude(request.getPath().toString())){
            // 需要放行
            return chain.filter(exchange);
        }
        // 3. 获取到 token
        String token = null;
        List<String> tokens = request.getHeaders().get("authorization");
        if(tokens != null && !tokens.isEmpty()){
            token = tokens.get(0);
        }
        // 4. 校验并且解析 token
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token);
        } catch(UnauthorizedException e){
            // 401 表示没有登录
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 5. 传递用户信息
        System.out.println("userId = " + userId);
        String userInfo = userId.toString();
        ServerWebExchange webExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        // 6. 放行
        return chain.filter(webExchange);
    }

    private boolean isExclude(String path) {
        for (String excludePath : authProperties.getExcludePaths()) {
            if(antPathMatcher.match(excludePath,path)){
                return true;  // 表示不用拦截了
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
