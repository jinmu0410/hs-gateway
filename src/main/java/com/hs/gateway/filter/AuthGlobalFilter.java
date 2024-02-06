package com.hs.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * TODO
 * 自定义全局拦截器
 *
 * @Author jinmu
 * @Date 2024/2/4 17:17
 */
@Configuration
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //暂时不做处理
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
