/*
 * Copyright 2020 Bosch.IO GmbH. All rights reserved.
 */
package com.bosch.iothub.examples;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.client.impl.HonoConnectionImpl;
import org.eclipse.hono.config.ClientConfigProperties;
import org.eclipse.hono.connection.ConnectionFactory;
import org.eclipse.hono.connection.impl.ConnectionFactoryImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Example application.
 */
@Configuration
public class AppConfiguration {

    private static final int DEFAULT_ADDRESS_RESOLUTION_TIMEOUT = 2000;

    /**
     * Exposes a Vert.x instance as a Spring bean.
     *
     * @return The Vert.x instance.
     */
    @Bean
    public Vertx vertx() {
        VertxOptions options = new VertxOptions()
                .setAddressResolverOptions(new AddressResolverOptions()
                        .setCacheNegativeTimeToLive(0) // discard failed DNS lookup results immediately
                        .setCacheMaxTimeToLive(0) // support DNS based service resolution
                        .setRotateServers(true)
                        .setQueryTimeout(DEFAULT_ADDRESS_RESOLUTION_TIMEOUT));
        return Vertx.vertx(options);
    }

    /**
     * Exposes client configuration properties as a Spring bean.
     *
     * @return The properties.
     */
    @ConfigurationProperties(prefix = "hono.client")
    @Bean
    public ClientConfigProperties honoClientConfig() {
        return new ClientConfigProperties();
    }

    /**
     * Exposes a factory for connections to the Hono server
     * as a Spring bean.
     *
     * @return The connection factory.
     */
    @Bean
    public ConnectionFactory honoConnectionFactory() {
        return new ConnectionFactoryImpl(vertx(), honoClientConfig());
    }


    /**
     * Exposes a {@code HonoConnection} as a Spring bean.
     *
     * @return The Hono connection.
     */
    @Bean
    public HonoConnection honoConnection() {
        return new HonoConnectionImpl(vertx(), honoConnectionFactory(), honoClientConfig());
    }

    /**
     * Exposes a {@code ApplicationClientFactory} as a Spring bean.
     * @return The Application client factory
     */
    @Bean
    public ApplicationClientFactory clientFactory() {
        return ApplicationClientFactory.create(honoConnection());
    }
}
