/*
 * Bosch SI Example Code License Version 1.0, January 2016
 *
 * Copyright 2016 Bosch Software Innovations GmbH ("Bosch SI"). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * BOSCH SI PROVIDES THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE
 * QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
 * NECESSARY SERVICING, REPAIR OR CORRECTION. THIS SHALL NOT APPLY TO MATERIAL DEFECTS AND DEFECTS OF TITLE WHICH BOSCH
 * SI HAS FRAUDULENTLY CONCEALED. APART FROM THE CASES STIPULATED ABOVE, BOSCH SI SHALL BE LIABLE WITHOUT LIMITATION FOR
 * INTENT OR GROSS NEGLIGENCE, FOR INJURIES TO LIFE, BODY OR HEALTH AND ACCORDING TO THE PROVISIONS OF THE GERMAN
 * PRODUCT LIABILITY ACT (PRODUKTHAFTUNGSGESETZ). THE SCOPE OF A GUARANTEE GRANTED BY BOSCH SI SHALL REMAIN UNAFFECTED
 * BY LIMITATIONS OF LIABILITY. IN ALL OTHER CASES, LIABILITY OF BOSCH SI IS EXCLUDED. THESE LIMITATIONS OF LIABILITY
 * ALSO APPLY IN REGARD TO THE FAULT OF VICARIOUS AGENTS OF BOSCH SI AND THE PERSONAL LIABILITY OF BOSCH SI'S EMPLOYEES,
 * REPRESENTATIVES AND ORGANS.
 */
package com.bosch.iot.hub.examples.application.helloworld;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosch.iot.hub.client.DefaultIotHubClient;
import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.IotHubClientBuilder;
import com.bosch.iot.hub.client.handler.ConsumerRegistration;
import com.bosch.iot.hub.model.message.MessageSender;
import com.bosch.iot.hub.model.message.Payload;

/**
 * This example shows how to use the BOSCH IoT Hub Java Client for easily registering and unregistering a consumer for
 * inbound messages.
 */
public final class HelloWorldApplication
{

   /**
    * URL of the BOSCH IoT Hub cloud service.
    */
   private static final URI BOSCH_IOT_HUB_ENDPOINT_URI = URI.create("wss://hub.apps.bosch-iot-cloud.com");

   private static final String SOLUTION_ID = "<your-solution-id>"; // TODO insert your Solution ID here
   private static final String SOLUTION_API_TOKEN = "<your-solution-APIToken>";  // TODO insert your Solution API Token here
   private static final String CONSUMER_CLIENT_ID = SOLUTION_ID + ":consumer";

   private static final URL KEYSTORE_LOCATION = HelloWorldApplication.class.getResource("/HubClient.jks");
   private static final String KEYSTORE_PASSWORD = "<your-keystore-password>"; // TODO insert your keystore password
   private static final String ALIAS = "<ALIAS>"; // TODO insert your alias here
   private static final String ALIAS_PASSWORD = "<your-alias-password>"; // TODO insert your alias password here
   private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldApplication.class);

   private static final long EXECUTION_TIME_MILLIS = 60000;

   /**
    * Uses the BOSCH IoT Hub Java Client to register a consumer for inbound messages.
    */
   public static void main(final String[] args) throws Exception
   {
      final IotHubClient iotHubClient = createNewIntegrationClient();

      // In order to work with the client a connection has to be established first.
      iotHubClient.connect();

      // Create and register a new consumer.
      final ConsumerRegistration consumerRegistration = iotHubClient.consume(inboundMessage -> {
         final String payload = inboundMessage.getPayload() //
                 .map(Payload::getContentAsByteArray) //
                 .map(payloadBytes -> new String(payloadBytes, StandardCharsets.UTF_8)) //
                 .orElse(null);

         final MessageSender messageSender = inboundMessage.getSender();

         LOGGER.info("Received message with payload <{}> from client <{}>.", payload, messageSender.getIdentifier());
      });

      /*
       * Block the main thread. The consumer runs in its own thread and thus still gets notified about received
       * messages.
       */
      Thread.sleep(EXECUTION_TIME_MILLIS);

      // Unregister the consumer.
      consumerRegistration.unregister();

      // This step must always be performed in order to terminate the client.
      iotHubClient.destroy();
   }

   private static IotHubClient createNewIntegrationClient() throws URISyntaxException
   {
      LOGGER.info("Creating Hub Integration Client for client ID <{}>.", CONSUMER_CLIENT_ID);

      /*
       * Provide required configuration (authentication configuration and HUB URI).
       * Proxy configuration is optional and can be added if needed.
       */
      final IotHubClientBuilder.OptionalPropertiesStep builder = DefaultIotHubClient.newBuilder() //
              .endPoint(BOSCH_IOT_HUB_ENDPOINT_URI) //
              .keyStore(KEYSTORE_LOCATION.toURI(), KEYSTORE_PASSWORD) //
              .alias(ALIAS, ALIAS_PASSWORD) //
              .clientId(CONSUMER_CLIENT_ID) //
              .apiToken(SOLUTION_API_TOKEN); //
      // .proxy(URI.create("http://" + <proxy-host> + ":" + <proxy port>)); //

      return builder.build();
   }

}