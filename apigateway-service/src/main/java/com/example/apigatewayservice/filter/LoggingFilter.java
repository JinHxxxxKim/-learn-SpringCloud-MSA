package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
//        // Logging Filter
//        return (exchange, chain) -> {
//            ServerHttpRequest request = exchange.getRequest();
//            ServerHttpResponse response = exchange.getResponse();
//            log.info("Logging filter: Base Message -> {}", config.getBaseMessage());
//
//            if (config.isPreLogger()) {
//                log.info("Logging filter START: request ID -> {}", request.getId());
//            }
//
//            // Custom Post Filter
//            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//                if (config.isPostLogger()) {
//                    log.info("Logging filter END: http status code -> {}", response.getStatusCode());
//                }
//            }));
//        };
        // ServerWebExchange => ServerHttpRequest, ServerHttpResponse를 사용할 수 있도록 해주는 객체
        // => 원하는 req, resp 객체를 얻어온다.
        // GatewayFilterChain => 다양한 필터들에 대해 연결시켜서 작동할 수 있도록 한다.

        GatewayFilter filter = new OrderedGatewayFilter(((exchange, chain) -> {
            // 구현시킬 필터 내용 filter()라는 메소드에 들어갈 내용
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            log.info("Logging filter: Base Message -> {}", config.getBaseMessage());

            if (config.isPreLogger()) {
                log.info("Logging filter START: request ID -> {}", request.getId());
            }

            // Custom Post Filter
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("Logging filter END: http status code -> {}", response.getStatusCode());
                }
            }));
        }), Ordered.LOWEST_PRECEDENCE);
        // Ordered.HIGHEST_PRECEDENCE: 가장 높은 우선순위로 지정한다: 가장 먼저 실행함
        return filter;
    }

    @Data
    public static class Config {
        //todo: CustomFilter의 설정정보 기입
        private String BaseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }


}
