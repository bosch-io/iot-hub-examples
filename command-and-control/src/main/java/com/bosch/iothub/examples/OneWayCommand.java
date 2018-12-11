/*
 * Copyright 2018 Bosch Software Innovations GmbH ("Bosch SI"). All rights reserved.
 */
package com.bosch.iothub.examples;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.proton.ProtonClientOptions;
import org.eclipse.hono.client.CommandClient;
import org.eclipse.hono.client.HonoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class OneWayCommand {
    private static final Logger LOG = LoggerFactory.getLogger(OneWayCommand.class);

    @Value(value = "${tenant.id}")
    protected String tenantId;

    @Value(value = "${device.id}")
    protected String deviceId;

    private final HonoClient client;
    private final ApplicationContext appContext;

    @Autowired
    public OneWayCommand(HonoClient client, ApplicationContext appContext) {
        this.client = client;
        this.appContext = appContext;
    }

    @PostConstruct
    private void start() {
        LOG.info("Connecting to IoT Hub messaging endpoint...");
        client.connect(new ProtonClientOptions()).compose(connectedClient -> {
            LOG.info("Connected to IoT Hub messaging endpoint.");
            final Future<CommandClient> commandClientFuture = connectedClient.getOrCreateCommandClient(tenantId, deviceId);
            commandClientFuture.setHandler(commandClientResult -> {
                final CommandClient commandClient = commandClientResult.result();
                sendOneWayCommand(commandClient);
            });
            return Future.succeededFuture();
        }).otherwise(connectException -> {
            LOG.info("Connecting or creating a command client failed with an exception: ", connectException);
            return null;
        });
    }

    private void sendOneWayCommand(CommandClient commandClient) {
        LOG.info("Send command (one-way mode) to device '{}'", deviceId);
        commandClient.setRequestTimeout(TimeUnit.SECONDS.toMillis(2)); // increase to avoid errors with 200 ms default timeout
        final Buffer data = Buffer.buffer("on");
        final Future<Void> commandResult = commandClient.sendOneWayCommand("switchLight", data);
        commandResult.setHandler(result -> {
            if (result.succeeded()) {
                LOG.info("Command successfully send");
                SpringApplication.exit(appContext, (ExitCodeGenerator) () -> 0);
            } else {
                LOG.error("Command not successfully send: {}", result.cause().getMessage());
                SpringApplication.exit(appContext, (ExitCodeGenerator) () -> 1);
            }
        });
    }
}
