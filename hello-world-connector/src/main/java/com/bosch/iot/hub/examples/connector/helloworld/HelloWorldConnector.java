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
package com.bosch.iot.hub.examples.connector.helloworld;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosch.iot.hub.client.DefaultIotHubClient;
import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.IotHubClientBuilder;
import com.bosch.iot.hub.model.acl.AccessControlList;
import com.bosch.iot.hub.model.acl.AclEntry;
import com.bosch.iot.hub.model.acl.Permission;
import com.bosch.iot.hub.model.message.Message;
import com.bosch.iot.hub.model.message.Payload;
import com.bosch.iot.hub.model.topic.TopicPath;

/**
 * This example shows how simple it is to create new Topics and send messages with the BOSCH IoT Hub Java Client.
 */
public final class HelloWorldConnector
{

   /**
    * URL of the BOSCH IoT Hub cloud service.
    */
   private static final URI BOSCH_IOT_HUB_ENDPOINT_URI = URI.create("wss://hub.apps.bosch-iot-cloud.com");

   private static final String SOLUTION_ID = "<your-solution-id>"; // TODO insert your Solution ID here
   private static final String SOLUTION_API_TOKEN = "<your-solution-APIToken>";
      // TODO insert your Solution API Token here
   private static final String CLIENT_ID = SOLUTION_ID + ":connector";
   private static final String CONSUMER_CLIENT_ID = SOLUTION_ID + ":consumer";

   private static final URL KEYSTORE_LOCATION = HelloWorldConnector.class.getResource("/HubClient.jks");
   private static final String KEYSTORE_PASSWORD = "<your-keystore-password>";
      // TODO insert your keystore password here
   private static final String ALIAS = "<ALIAS>"; // TODO insert your alias here
   private static final String ALIAS_PASSWORD = "<your-alias-password>"; // TODO insert your alias password here

   private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldConnector.class);

   // ACLs (Access Control List) are used to define permissions on Topics.
   private static final AclEntry CONNECTOR_ACL_ENTRY =
           AclEntry.of(CLIENT_ID, Permission.ADMINISTRATE, Permission.RECEIVE, Permission.SEND);
   private static final AclEntry CONSUMER_ACL_ENTRY = AclEntry.of(CONSUMER_CLIENT_ID, Permission.RECEIVE);
   private static final AccessControlList TOPIC_ACL = AccessControlList.of(CONNECTOR_ACL_ENTRY, CONSUMER_ACL_ENTRY);

   private static final long DEFAULT_TIMEOUT = 5;

   /**
    * Creates a Topic and sends a "Hello World" message to that Topic using the IoT Hub Java Client.
    */
   public static void main(final String[] args) throws Exception
   {
      final IotHubClient iotHubClient = createNewIntegrationClient();

      // In order to work with the client a connection has to be established first.
      iotHubClient.connect();

      // Using a dedicated type for Topic paths; simple strings would work as well.
      final TopicPath rootTopicPath = TopicPath.of(SOLUTION_ID);
      final TopicPath myHouseTopicPath = rootTopicPath.append("myHouse");
      final TopicPath myGardenTopicPath = myHouseTopicPath.append("myGarden");
      final TopicPath mowerTopicPath = myGardenTopicPath.append("mower");
      // mowerTopicPath is "<SOLUTION_ID>/myHouse/myGarden/mower"

      // Create new Topics with the pre-defined ACL (recursive Topic creation is not possible)
      iotHubClient.createTopic(rootTopicPath, TOPIC_ACL).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
      iotHubClient.createTopic(myHouseTopicPath, TOPIC_ACL).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
      iotHubClient.createTopic(myGardenTopicPath, TOPIC_ACL).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
      iotHubClient.createTopic(mowerTopicPath, TOPIC_ACL).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      // Send the message.
      iotHubClient.send(Message.of(mowerTopicPath, Payload.of("Hello World"))).thenAccept(SendSuccess ->
      {
         LOGGER.info("Successfully send the message to topic <{}>.", mowerTopicPath);
      });

      // This step must always be performed in order to terminate the client.
      iotHubClient.destroy();
   }

   private static IotHubClient createNewIntegrationClient() throws URISyntaxException
   {
      LOGGER.info("Creating Hub Integration Client for client ID <{}>.", CLIENT_ID);

      /*
       * Provide required configuration (authentication configuration and HUB URI).
       * Proxy configuration is optional and can be added if needed.
       */
      final IotHubClientBuilder.OptionalPropertiesStep builder = DefaultIotHubClient.newBuilder() //
         .keyStore(KEYSTORE_LOCATION.toURI(), KEYSTORE_PASSWORD) //
         .alias(ALIAS, ALIAS_PASSWORD) //
         .clientId(CLIENT_ID) //
         .apiToken(SOLUTION_API_TOKEN) //
         .endPoint(BOSCH_IOT_HUB_ENDPOINT_URI);
      // .proxy(URI.create("http://" + <proxy-host> + ":" + <proxy port>)); //

      return builder.build();
   }

}
