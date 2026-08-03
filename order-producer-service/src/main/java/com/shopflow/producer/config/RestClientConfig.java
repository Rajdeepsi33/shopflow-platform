package com.shopflow.producer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * HTTP client for the product catalogue.
 *
 * The base URL is externalised so tests can point it at WireMock.
 * Connection and read timeouts are set below the Resilience4j time
 * limiter so a hung socket cannot outlive the 2s budget.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient catalogRestClient(@Value("${shopflow.catalog.base-url}") String baseUrl) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(1));
        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}