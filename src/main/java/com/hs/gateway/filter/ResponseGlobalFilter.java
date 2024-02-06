package com.hs.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.hs.gateway.res.ResResult;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

/**
 * TODO
 * 响应拦截器
 *
 * @Author jinmu
 * @Date 2024/2/4 17:20
 */
@Component
public class ResponseGlobalFilter implements GlobalFilter, Ordered {
    private final Logger log = LoggerFactory.getLogger(ResponseGlobalFilter.class);


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取响应对象
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                //记录响应日志
                long end = System.currentTimeMillis();
                long start = Long.parseLong(exchange.getAttribute("startTime").toString());
                long useTime = end - start;
                if (body instanceof Flux) {
                    // 获取响应 ContentType
                    String responseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                    // 记录 JSON 格式数据的响应体
                    if (!StringUtils.isEmpty(responseContentType) && responseContentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                        // 解决返回体分段传输
                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                            DataBuffer join = dataBufferFactory.join(dataBuffers);
                            byte[] content = new byte[join.readableByteCount()];
                            join.read(content);
                            DataBufferUtils.release(join);
                            String responseData = new String(content, StandardCharsets.UTF_8);

                            String responseStr = responseData.replaceAll("\n", "").replaceAll("\t", "");
                            responseStr = responseStr.length() > 500 ? responseStr.substring(500) : responseStr;

                            log.info("=>> END: Time: {}ms", useTime);
                            log.info("RESPONSE INFO = {}", responseStr);

                            ResResult resResult = JSON.parseObject(responseStr, ResResult.class);
                            //解析为resResult非200抛出异常
                            if (resResult.getData() != null && resResult.getCode() != 200 && !(boolean) resResult.getData()) {
                                throw new RuntimeException(resResult.getMessage());
                            }

                            return bufferFactory.wrap(JSON.toJSONBytes(resResult));
                        }));
                    }
                }

                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
