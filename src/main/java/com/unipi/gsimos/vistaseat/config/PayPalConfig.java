package com.unipi.gsimos.vistaseat.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that provides a {@link PayPalHttpClient} for the PayPal Sandbox.
 * <p>
 * Credentials are read from application properties:
 * <ul>
 *   <li><code>paypal.client-id</code></li>
 *   <li><code>paypal.client-secret</code></li>
 * </ul>
 * Use this client to create/capture PayPal orders in non-production environments.
 */
@Configuration
public class PayPalConfig {

    @Value("${paypal.client-id}") String clientId;
    @Value("${paypal.client-secret}") String clientSecret;

    @Bean
    public PayPalHttpClient payPalClient(){

        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("Missing PayPal credentials. Set paypal.client-id and paypal.client-secret.");
        }

        PayPalEnvironment env = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        return new PayPalHttpClient(env);
    }
}