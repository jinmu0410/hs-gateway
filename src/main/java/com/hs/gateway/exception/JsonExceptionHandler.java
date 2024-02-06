package com.hs.gateway.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @Author jinmu
 * @Date 2024/2/4 23:18
 */
public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {

    private final static Logger log = LoggerFactory.getLogger(JsonExceptionHandler.class);

    public JsonExceptionHandler(ErrorAttributes errorAttributes, WebProperties resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources.getResources(), errorProperties, applicationContext);
    }

    /**
     * 获取异常属性
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = super.getError(request);
        return this.buildMessage(request, error);
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     *
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     *
     * @param errorAttributes
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return HttpStatus.OK.value();
    }

    /**
     * 构建异常响应信息
     *
     * @param request
     * @param throwable
     * @return
     */
    private Map<String, Object> buildMessage(ServerRequest request, Throwable throwable) {
        Map<String, Object> map = new HashMap<>(8);
        log.error("[网关异常信息]请求路径：{}，异常信息：{}", request.path(), throwable.getMessage());
        //封装错误信息
        map.put("message", throwable.getMessage());
        map.put("data", false);

        return map;
    }
}
