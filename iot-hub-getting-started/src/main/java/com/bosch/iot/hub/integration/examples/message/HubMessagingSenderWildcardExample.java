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
package com.bosch.iot.hub.integration.examples.message;

import static com.bosch.iot.hub.model.acl.Permission.ADMINISTRATE;
import static com.bosch.iot.hub.model.acl.Permission.RECEIVE;
import static com.bosch.iot.hub.model.acl.Permission.SEND;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.SendSuccess;
import com.bosch.iot.hub.integration.examples.util.HubClientUtil;
import com.bosch.iot.hub.model.acl.AccessControlList;
import com.bosch.iot.hub.model.acl.AclBuilder;
import com.bosch.iot.hub.model.message.Message;
import com.bosch.iot.hub.model.message.Payload;
import com.bosch.iot.hub.model.topic.TopicPath;

/**
 * Preconditions of running the example (first check {@link HubMessagingReceiverExample} to be able to receive messages
 * sent within this example) :
 * <ol>
 * <li>Register one solution as message sender, get solution_id and upload the public key from /HubClient.jks (see steps
 * described at <a href="https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking">Book the
 * Bosch IoT Hub cloud service</a>)</li>
 * <li>Use sender solution_id as your system property "SENDER_SOLUTION_ID"</li>
 * <li>Use receiver solution_id generated/used in {@link HubMessagingReceiverExample} as your system property
 * "RECEIVER_SOLUTION_ID"</li>
 * <li>Configure system property "HUB_CLOUD_ENDPOINT", using actual Websocket endpoint of IoT Hub Service</li>
 * <li>Configure system property "PROXY_URI" if you have one, using format http://host:port</li>
 * </ol>
 * Examples of System Properties: <br/>
 * <strong> -DSENDER_SOLUTION_ID=xx -DRECEIVER_SOLUTION_ID=xx -DHUB_CLOUD_ENDPOINT=wss://xx.com
 * -DPROXY_URI=http://xx.com</strong>
 */
public final class HubMessagingSenderWildcardExample
{

   private static final AccessControlList TOPIC_ACL = AclBuilder.newInstance() //
      .set(HubClientUtil.RECEIVER_SOLUTION_CLIENT_ID, RECEIVE) //
      .set(HubClientUtil.SENDER_SOLUTION_CLIENT_ID, ADMINISTRATE, RECEIVE, SEND) //
      .build();

   public static void main(final String[] args)
      throws InterruptedException, ExecutionException, TimeoutException, IOException
   {
      // Create client
      final IotHubClient senderClient =
         HubClientUtil.initSolutionClient(HubClientUtil.SENDER_SOLUTION_CLIENT_ID, HubClientUtil.CLIENT_API_TOKEN);
      senderClient.connect();

      // Create solution Topic
      final TopicPath solutionTopicPath = TopicPath.of(HubClientUtil.SOLUTION_TOPIC);
      senderClient.createTopic(solutionTopicPath).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      final TopicPath wildcardTopicPath = solutionTopicPath.append("*");
      final TopicPath subTopicPath = wildcardTopicPath.append("subTopic");

      // Sender defines ACL on wildcard Topic for high level ACL management
      senderClient.createTopic(wildcardTopicPath, TOPIC_ACL).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
      senderClient.createTopic(subTopicPath).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      // Sending is only allowed to Topics without wildcards
      final TopicPath arbitraryTopicPath = solutionTopicPath.append("someArbitraryTopic");
      final TopicPath targetTopicPath = arbitraryTopicPath.append(subTopicPath);

      // Read console input and send them to solution Topic
      final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
      String consoleInput;
      while ((consoleInput = consoleReader.readLine()) != null && !consoleInput.isEmpty())
      {
         // Send console input to target Topic
         final Message message = Message.of(targetTopicPath, Payload.of(consoleInput));
         final CompletableFuture<SendSuccess> sendPromise = senderClient.send(message);
         sendPromise.get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
      }
      consoleReader.close();

      // Delete Topic
      senderClient.deleteTopic(HubClientUtil.SOLUTION_TOPIC).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      // Clean up
      senderClient.destroy();
   }

}
