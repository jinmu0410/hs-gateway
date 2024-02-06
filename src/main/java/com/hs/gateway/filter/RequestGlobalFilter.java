package com.hs.gateway.filter;

import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


/**
 * TODO
 * 请求拦截器
 *
 * @Author jinmu
 * @Date 2024/2/4 17:19
 */
@Component
public class RequestGlobalFilter implements GlobalFilter, Ordered {
    /**
     * default HttpMessageReader
     */
    private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    private final Logger log = LoggerFactory.getLogger(RequestGlobalFilter.class);
    private final String ip = null;
    private ServerHttpRequest request = null;
    private MediaType contentType = null;
    private HttpHeaders headers = null;
    private String path = null;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 设置开始时间戳
        exchange.getAttributes().put("startTime", System.currentTimeMillis());
        // 重新设置请求的IP
        // 写入客户端ip，否则在下游服务获取的ip为网关ip
        ServerHttpRequest newRequest = exchange.getRequest().mutate().header("CLIENT_IP", ip).build();
        exchange = exchange.mutate().request(newRequest).build();

        // 初始化request并且获取path
        request = exchange.getRequest();
        headers = request.getHeaders();
        contentType = headers.getContentType();
        path = request.getPath().pathWithinApplication().value();

        /*
         * todo 记录
         */
        log.info("=>> Start：HttpMethod：{},Url：{}", request.getMethod(), request.getURI().getRawPath());
        if (request.getMethod() == HttpMethod.GET) {
            // 记录请求的参数信息 针对GET 请求
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                builder.append(entry.getKey()).append("=").append(StringUtils.join(entry.getValue(), ",")).append(",");
            }
            log.info("MethodParam:{}", builder);

        }
        if (request.getMethod() == HttpMethod.POST || request.getMethod() == HttpMethod.PUT || request.getMethod() == HttpMethod.DELETE) {
            Mono<Void> voidMono = null;
            if (contentType != null) {
                if (StringUtils.contains(contentType.toString(), MediaType.APPLICATION_JSON.toString())) {
                    voidMono = readBody(exchange, chain);
                }
                return voidMono;
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -3;
    }


    /**
     * ReadJsonBody
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> readBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    /*
                     * read the body Flux<DataBuffer>, and release the buffer
                     * //TODO when SpringCloudGateway Version Release To G.SR2,this can be update with the new version's feature
                     * see PR https://github.com/spring-cloud/spring-cloud-gateway/pull/1095
                     */
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                        DataBuffer buffer =
                                exchange.getResponse().bufferFactory().wrap(bytes);
                        DataBufferUtils.retain(buffer);
                        return Mono.just(buffer);
                    });
                    /**
                     * repackage ServerHttpRequest
                     */
                    ServerHttpRequest mutatedRequest =
                            new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return cachedFlux;
                                }
                            };
                    /**
                     * mutate exchage with new ServerHttpRequest
                     */
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    /**
                     * read body string with default messageReaders
                     */
                    return ServerRequest.create(mutatedExchange, messageReaders)
                            .bodyToMono(String.class).doOnNext(objectValue -> {
                                //请求参数
                                log.info("MethodParam:{}", objectValue);
                            }).then(chain.filter(mutatedExchange));
                });
    }

}
