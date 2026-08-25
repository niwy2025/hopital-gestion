package com.hopital.patient.infrastructure.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpAccessLogConfiguration {

    @Bean
    HttpAccessLogFilter httpAccessLogFilter() {
        return new HttpAccessLogFilter();
    }
}
