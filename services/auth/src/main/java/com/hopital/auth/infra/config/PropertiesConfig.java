package com.hopital.auth.infra.config;

import com.hopital.auth.application.config.AuthServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthServiceProperties.class)
public class PropertiesConfig {
}
