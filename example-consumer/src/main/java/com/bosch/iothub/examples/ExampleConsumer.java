/*
 * Copyright 2020 Bosch.IO GmbH. All rights reserved.
 */
package com.bosch.iothub.examples;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;
import org.eclipse.hono.client.ApplicationClientFactory;
import org.eclipse.hono.client.DisconnectListener;
import org.eclipse.hono.client.HonoConnection;
import org.eclipse.hono.client.MessageConsumer;
import org.eclipse.hono.util.MessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ExampleConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ExampleConsumer.class);
    private static final int RECONNECT_INTERVAL_MILLIS = 1000;

    @Value(value = "${tenant.id}")
    protected String tenantId;

    @Autowired
    private Vertx vertx;

    @Autowired
    private ApplicationClientFactory clientFactory;

    private long reconnectTimerId = -1;

    void setClientFactory(ApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @PostConstruct
    private void start() {
        connectWithRetry();
    }

    /**
     * Try to connect Hono client infinitely regardless of errors which may occur,
     * even if the Hono client itself is incorrectly configured (e.g. wrong credentials).
     * This is to ensure that client tries to re-connect in unforeseen situations.
     */
    private void connectWithRetry() {
        clientFactoryConnect(this::onDisconnect).compose(connection -> {
            LOG.info("Connected to IoT Hub messaging endpoint.");
            return createTelemetryConsumer().compose(createdConsumer -> {
                LOG.info("Consumer ready [tenant: {}, type: telemetry]. Hit ctrl-c to exit...", tenantId);
                return Future.succeededFuture();
            });
        }).otherwise(connectException -> {
            LOG.info("Connecting or creating a consumer failed with an exception: ", connectException);
            LOG.info("Reconnecting in {} ms...", RECONNECT_INTERVAL_MILLIS);

            // As timer could be triggered by detach or disconnect we need to ensure here that timer runs only once
            vertx.cancelTimer(reconnectTimerId);
            reconnectTimerId = vertx.setTimer(RECONNECT_INTERVAL_MILLIS, timerId -> connectWithRetry());
            return null;
        });
    }

    Future<HonoConnection> clientFactoryConnect(DisconnectListener<HonoConnection> disconnectHandler) {
        LOG.info("Connecting to IoT Hub messaging endpoint...");
        clientFactory.addDisconnectListener(disconnectHandler);
        return clientFactory.connect();
    }

    Future<MessageConsumer> createTelemetryConsumer() {
        LOG.info("Creating telemetry consumer...");
        return clientFactory.createTelemetryConsumer(tenantId, this::handleMessage, this::onDetach);
    }

    private void onDisconnect(final HonoConnection connection) {
        LOG.info("Client got disconnected. Reconnecting...");
        connectWithRetry();
    }

    private void onDetach(Void event) {
        LOG.info("Client got detached. Reconnecting...");
        connectWithRetry();
    }

    private void handleMessage(final Message msg) {
        final String deviceId = MessageHelper.getDeviceId(msg);
        String content = ((Data) msg.getBody()).getValue().toString();

        LOG.info("Received message [device: {}, content-type: {}]: {}", deviceId, msg.getContentType(), content);
        LOG.info("... with application properties: {}", msg.getApplicationProperties());
    }
}
