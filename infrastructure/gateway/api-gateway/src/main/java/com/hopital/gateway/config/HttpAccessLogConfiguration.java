package com.hopital.gateway.config;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;

@Configuration
public class HttpAccessLogConfiguration {

    private static final Logger log = LoggerFactory.getLogger("http.access");

    @Bean
    GlobalFilter httpAccessLogFilter() {
        return (exchange, chain) -> {
            long startedAt = System.nanoTime();
            return chain.filter(exchange).doFinally(signalType -> {
                HttpStatusCode status = exchange.getResponse().getStatusCode();
                long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
                log.info(
                        "HTTP {} {} -> {} ({} ms)",
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getPath().value(),
                        status == null ? 500 : status.value(),
                        durationMs);
            });
        };
    }
}
