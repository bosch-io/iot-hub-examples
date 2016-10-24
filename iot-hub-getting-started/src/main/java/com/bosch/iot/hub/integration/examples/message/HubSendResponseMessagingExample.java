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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.SendSuccess;
import com.bosch.iot.hub.integration.examples.util.HubClientUtil;
import com.bosch.iot.hub.model.acl.AccessControlList;
import com.bosch.iot.hub.model.acl.AclEntry;
import com.bosch.iot.hub.model.acl.AuthorizationSubject;
import com.bosch.iot.hub.model.message.InboundResponseMessage;
import com.bosch.iot.hub.model.message.Message;
import com.bosch.iot.hub.model.message.Payload;
import com.bosch.iot.hub.model.message.ResponseMessage;

/**
 * Preconditions of running the example :
 * <ol>
 * <li>Register one solution as message sender, get solution_id, and upload the public key from /HubClient.jks (see steps described at
 * <a href="https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking">Book the Bosch IoT Hub cloud service</a>)</li>
 * <li>Register one solution as message receiver, get solution_id, and upload the public key from /HubClient.jks (see steps described at
 * <a href="https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking">Book the Bosch IoT Hub cloud service</a>)</li>
 * <li>Use sender solution_id as your system property "SENDER_SOLUTION_ID"</li>
 * <li>Use receiver solution_id as your system property "RECEIVER_SOLUTION_ID"</li>
 * <li>Configure system property "HUB_CLOUD_ENDPOINT", using actual Websocket endpoint of IoT Hub Service</li>
 * <li>Configure system property "PROXY_URI" if you have one, using format http://host:port</li>
 * </ol>
 * Examples of System Properties:
 * <br/>
 * <strong> -DSENDER_SOLUTION_ID=xx -DRECEIVER_SOLUTION_ID=xx -DHUB_CLOUD_ENDPOINT=wss://xx.com -DPROXY_URI=http://xx.com</strong>
 */
public class HubSendResponseMessagingExample
{
   private static final Logger LOGGER = LoggerFactory.getLogger(HubSendResponseMessagingExample.class);

   private static final AclEntry RECEIVER_ACL = AclEntry.of(AuthorizationSubject.of(HubClientUtil.RECEIVER_SOLUTION_CLIENT_ID), RECEIVE);
   private static final AclEntry SENDER_ACL =
      AclEntry.of(AuthorizationSubject.of(HubClientUtil.SENDER_SOLUTION_CLIENT_ID), ADMINISTRATE, RECEIVE, SEND);

   private static final AccessControlList TOPIC_ACLS = AccessControlList.of(RECEIVER_ACL, SENDER_ACL);

   public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, IOException
   {
      // Create sender client
      IotHubClient senderClient =
         HubClientUtil.initSolutionClient(HubClientUtil.SENDER_SOLUTION_CLIENT_ID, HubClientUtil.CLIENT_API_TOKEN);
      senderClient.connect();

      // Create receiver
      IotHubClient receiverClient =
         HubClientUtil.initSolutionClient(HubClientUtil.RECEIVER_SOLUTION_CLIENT_ID, HubClientUtil.CLIENT_API_TOKEN);
      receiverClient.connect();

      receiverClient.consume("requestConsumer", inboundMessage ->
      {
         try
         {
            LOGGER.info("[RECEIVER] Received message with id <{}> from sender <{}> --- Sending response message as acknowledgement...",
               inboundMessage.getId(), inboundMessage.getSender().getIdentifier());

            // build and send reply message
            final ResponseMessage responseMessage = ResponseMessage.newBuilder(inboundMessage) //
               .payload(String.format("[RECEIVER] Successfully received message with payload <%s>", inboundMessage.getPayload().get()))//
               .build();
            receiverClient.send(responseMessage).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
         }
         catch (InterruptedException | ExecutionException | TimeoutException e)
         {
            LOGGER.error("[RECEIVER] Could not send reply message to message sender [{}]", inboundMessage.getSender().getIdentifier());
         }
      });

      // Sender create Topic and give Receiver RECEIVE permission
      senderClient.createTopic(HubClientUtil.SOLUTION_TOPIC, TOPIC_ACLS).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      // Read console input and send them to solution topic
      final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
      String consoleInput = null;
      while ((consoleInput = consoleReader.readLine()) != null && !consoleInput.isEmpty())
      {
         // Send console input to solution Topic and register consumer for reply message
         final CompletableFuture<SendSuccess> sendFuture =
            senderClient.send(Message.of(HubClientUtil.SOLUTION_TOPIC, Payload.of(consoleInput)), "responseConsumer", asyncResult ->
            {
               final InboundResponseMessage responseMessage = asyncResult.getMessage();

               LOGGER.info("[SENDER] Received response message from receiver <{}> related to sent message with id <{}>",
                  responseMessage.getSender().getIdentifier(), responseMessage.getCorrelationId());
               if (responseMessage.getPayload().isPresent())
               {
                  final String responsePayload = new String(responseMessage.getPayload().get().getContentAsByteArray());
                  LOGGER.info("[SENDER] Response message payload <{}>", responsePayload);
               }
            });
         sendFuture.get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
      }
      consoleReader.close();

      // Delete Topic
      senderClient.deleteTopic(HubClientUtil.SOLUTION_TOPIC).get(HubClientUtil.DEFAULT_TIMEOUT, TimeUnit.SECONDS);

      // Clean up
      senderClient.destroy();
      receiverClient.destroy();
   }
}
