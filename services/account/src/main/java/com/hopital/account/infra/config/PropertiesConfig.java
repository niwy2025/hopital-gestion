package com.hopital.account.infra.config;

import com.hopital.account.application.config.AccountSeedProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AccountSeedProperties.class)
public class PropertiesConfig {
}
