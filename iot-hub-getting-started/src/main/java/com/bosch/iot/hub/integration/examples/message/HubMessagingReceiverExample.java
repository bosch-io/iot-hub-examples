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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.integration.examples.util.HubClientUtil;

/**
 * Preconditions of running the example :
 * <ol>
 * <li>Register one solution as message receiver, get solution_id and upload the public key from /HubClient.jks (see steps described at
 * <a href="https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking">Book the Bosch IoT Hub cloud service</a>)</li>
 * <li>Use receiver solution_id as your system property "RECEIVER_SOLUTION_ID"</li>
 * <li>Configure system property "HUB_CLOUD_ENDPOINT", using actual Websocket endpoint of IoT Hub Service</li>
 * <li>Configure system property "PROXY_URI" if you have one, using format http://host:port</li>
 * </ol>
 * Examples of System Properties:
 * <br/>
 * <strong> -DRECEIVER_SOLUTION_ID=xx -DHUB_CLOUD_ENDPOINT=wss://xx.com -DPROXY_URI=http://xx.com</strong>
 */
public class HubMessagingReceiverExample
{
   public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, IOException
   {
      // Create receiver client
      IotHubClient receiverClient = HubClientUtil.initSolutionClient(HubClientUtil.RECEIVER_SOLUTION_CLIENT_ID, HubClientUtil.CLIENT_API_TOKEN);
      receiverClient.connect();

      // Add consumer to message
      receiverClient.consume(msg ->
      {
         // Do something with the message
         System.out.println(msg.getTopicPath().toString());
         System.out.println(msg.getPayload().get());
      });

      // Wait for console input to clean up the client
      System.in.read();
      receiverClient.destroy();
   }
}