/*
 * Copyright 2020 Bosch.IO GmbH. All rights reserved.
 */
package com.bosch.iothub.examples;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.config.ClientConfigProperties;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(VertxUnitRunner.class)
public class ExampleConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleConsumerTest.class);
    private static final String IOT_HUB_MESSAGING_HOST = "iot-hub.messaging.host";
    private static final String IOT_HUB_TENANT_ID = "iot-hub.tenant.id";
    private static final String HONO_CLIENT_USERNAME = "hono.client.user";
    private static final String HONO_CLIENT_PASSWORD = "hono.client.password";

    private static Vertx vertx;
    private static ApplicationClientFactory clientFactory;
    private static ExampleConsumer exampleConsumer;

    @BeforeClass()
    public static void beforeClass() {
        vertx = Vertx.vertx(new VertxOptions().setWarningExceptionTime(1000 * 1500000000)
                .setAddressResolverOptions(new AddressResolverOptions().setCacheMaxTimeToLive(0) // support DNS based service resolution
                        .setQueryTimeout(1000)));
        final ClientConfigProperties clientConfig = new ClientConfigProperties();
        clientConfig.setTlsEnabled(true);
        clientConfig.setHost(System.getProperty(IOT_HUB_MESSAGING_HOST));
        clientConfig.setUsername(System.getProperty(HONO_CLIENT_USERNAME));
        clientConfig.setPassword(System.getProperty(HONO_CLIENT_PASSWORD));
        clientConfig.setReconnectAttempts(0);
        clientFactory = ApplicationClientFactory.create(HonoConnection.newConnection(vertx, clientConfig));
        exampleConsumer = new ExampleConsumer();
        exampleConsumer.setClientFactory(clientFactory);
        exampleConsumer.setTenantId(System.getProperty(IOT_HUB_TENANT_ID));
    }

    @AfterClass
    public static void afterClass() {
        clientFactory.disconnect();
        vertx.close();
    }

    @Test
    public void testHonoClientConnection(final TestContext context) {
        final Async async = context.async();
        final String testName = "Hono Client: IoT Hub Connection Test.";

        LOG.info("[{}]: Starting...", testName);

        exampleConsumer.clientFactoryConnect(null).setHandler(clientConnected -> {
            context.assertTrue(clientConnected.succeeded(), "[" + testName +"]: Hono client connection attempt test failed.");
            LOG.info("[{}]: Hono client connection attempt test succeeded.", testName);
            clientFactory.isConnected().setHandler(result -> {
                context.assertTrue(result.succeeded(), "[" + testName +"]: Hono client failed to connect.");
                LOG.info("[{}]: Hono client connected.", testName);
                async.complete();
            });
        });
    }

    @Test
    public void testConsumerConnection(final TestContext context) {
        final Async async = context.async();
        final String testName = "Telemetry Consumer: IoT Hub Connection test.";

        LOG.info("[{}]: Starting...", testName);

        exampleConsumer.createTelemetryConsumer().setHandler(consumerCreated -> {
            context.assertTrue(consumerCreated.succeeded(), "[" + testName +"]: Telemetry consumer creation failed.");
            LOG.info("[{}]: Telemetry consumer successfully created.", testName);
            async.complete();
        });
    }

}