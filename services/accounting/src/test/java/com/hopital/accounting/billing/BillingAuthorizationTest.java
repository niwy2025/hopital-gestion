package com.hopital.accounting.billing;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.hopital.accounting.infra.integration.auth.AuthAccessScopeClient;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(BillingAuthorizationTest.Config.class)
class BillingAuthorizationTest {
    @Configuration @EnableMethodSecurity @Import(BillingController.class) static class Config {
        @Bean BillingService service(){return mock(BillingService.class);}
        @Bean AuthAccessScopeClient scopes(){return mock(AuthAccessScopeClient.class);}
    }
    @Autowired BillingController controller;
    Jwt jwt=Jwt.withTokenValue("test").header("alg","none").subject("user").claim("preferred_username","user").build();
    UUID id=UUID.randomUUID();
    void role(String name){SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,List.of(new SimpleGrantedAuthority("ROLE_"+name))));}
    @AfterEach void clear(){SecurityContextHolder.clearContext();}
    @Test void cashierCanReadButNotWrite(){role("CASHIER");assertThatCode(()->controller.get(id,jwt)).doesNotThrowAnyException();assertThatThrownBy(()->controller.charge(id,null,jwt)).isInstanceOf(AccessDeniedException.class);assertThatThrownBy(()->controller.create(null,jwt)).isInstanceOf(AccessDeniedException.class);}
    @Test void billingOfficerWritesChargesButNotTariffs(){role("BILLING_OFFICER");assertThatCode(()->controller.charge(id,null,jwt)).doesNotThrowAnyException();assertThatThrownBy(()->controller.tariff(id,null,jwt)).isInstanceOf(AccessDeniedException.class);}
    @Test void financeManagerConfiguresAndClinicalRolesCannotRead(){role("FINANCE_MANAGER");assertThatCode(()->controller.create(null,jwt)).doesNotThrowAnyException();for(String role:List.of("DOCTOR","NURSE","PHARMACIST","STEWARD")){role(role);assertThatThrownBy(()->controller.get(id,jwt)).isInstanceOf(AccessDeniedException.class);}}
}
