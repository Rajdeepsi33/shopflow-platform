package com.shopflow.consumer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "shopflow")
public class ShopflowProperties {

    private Customs customs = new Customs();
    private Express express = new Express();

    @Getter
    @Setter
    public static class Customs {
        private List<String> euCountries = List.of();
        private List<String> embargoed = List.of();
        private BigDecimal dutyRate = BigDecimal.ZERO;
    }

    @Getter
    @Setter
    public static class Express {
        private BigDecimal surchargeRate = BigDecimal.ZERO;
    }
}