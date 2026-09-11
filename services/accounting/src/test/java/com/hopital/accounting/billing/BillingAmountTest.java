package com.hopital.accounting.billing;
import static org.assertj.core.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
class BillingAmountTest {
    @Test void roundsOnceWithoutFloatingPoint(){assertThat(BillingService.amount(new BigDecimal("2.5"),new BigDecimal("10.25"))).isEqualTo(new BigDecimal("25.63"));assertThat(BillingService.amount(BigDecimal.ONE,BigDecimal.ZERO)).isEqualTo(new BigDecimal("0.00"));}
    @Test void rejectsInvalidQuantitiesAndOverflow(){for(String q:new String[]{"0","-1","1.0001","1000000"})assertThatThrownBy(()->BillingService.amount(new BigDecimal(q),BigDecimal.ONE)).isInstanceOf(ResponseStatusException.class);assertThatThrownBy(()->BillingService.amount(new BigDecimal("999999"),new BigDecimal("99999999999999.99"))).isInstanceOf(ResponseStatusException.class);assertThatThrownBy(()->BillingService.amount(BigDecimal.ONE,new BigDecimal("0.001"))).isInstanceOf(ResponseStatusException.class);}
}
