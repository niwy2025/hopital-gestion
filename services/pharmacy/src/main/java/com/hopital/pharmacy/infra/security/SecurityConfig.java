package com.hopital.pharmacy.infra.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**", "/internal/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/pharmacy/**").hasAnyRole("ADMIN", "PHARMACY_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/pharmacy/**").hasAnyRole("ADMIN", "PHARMACIST", "PHARMACY_MANAGER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /** Les rôles applicatifs sont synchronisés dans realm_access par Keycloak. */
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Object roles = realmAccess == null ? null : realmAccess.get("roles");
            if (roles instanceof Collection<?> values) {
                values.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return converter;
    }
}
