package com.hopital.accounting.application.service;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
class FixedAssetAmountTest {
    @Test void allMonthlyRoundingAddsUpToDepreciableBase() {
        for(int months:new int[]{1,3,12,60,1200}) {
            var a=Map.<String,Object>of("accountingValue",new BigDecimal("100.07"),"residualValue",new BigDecimal("10.00"),"usefulLifeMonths",months);
            BigDecimal sum=BigDecimal.ZERO;
            for(int i=1;i<=months;i++)sum=sum.add(FixedAssetService.amount(a,i));
            assertThat(sum).isEqualByComparingTo("90.07");
        }
    }
}
