package com.hopital.personnel.infrastructure.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpAccessLogConfiguration {

    @Bean
    FilterRegistrationBean<HttpAccessLogFilter> httpAccessLogFilterRegistration() {
        FilterRegistrationBean<HttpAccessLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpAccessLogFilter());
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }
}
