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
package com.bosch.iot.hub.integration.examples.util;

import java.net.URI;

import com.bosch.iot.hub.client.DefaultIotHubClient;
import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.IotHubClientBuilder;
import com.bosch.iot.hub.integration.examples.topic.HubTopicMgmtExample;


public final class HubClientUtil
{
   public static final String SOLUTION_CLIENT_ID = System.getProperty("SOLUTION_ID");
   public static final String SENDER_SOLUTION_CLIENT_ID = System.getProperty("SENDER_SOLUTION_ID");
   public static final String CLIENT_API_TOKEN = System.getProperty("CLIENT_API_TOKEN");
   public static final String RECEIVER_SOLUTION_CLIENT_ID = System.getProperty("RECEIVER_SOLUTION_ID");

   public static final URI HUB_CLOUD_ENDPOINT = URI.create(System.getProperty("HUB_CLOUD_ENDPOINT"));
   public static final URI KEY_STORE_LOCATION =
           URI.create(HubTopicMgmtExample.class.getResource("/HubClient.jks").toString());
   public static final String KEY_STORE_PASSWORD = "123456";
   public static final String ALIAS = "HUB";
   public static final String ALIAS_PASSWORD = "123456";
   public static final String PROXY_URI = System.getProperty("PROXY_URI");

   public static final String SOLUTION_TOPIC = "topic_management_solution_" + SOLUTION_CLIENT_ID;
   public static final String SOLUTION_SUBTOPIC = SOLUTION_TOPIC + "/sub_topic";

   public static final long DEFAULT_TIMEOUT = 20;

   public static IotHubClient initSolutionClient(final String solutionId, final String apiToken)
   {
      IotHubClientBuilder.OptionalPropertiesStep propertiesSettable = DefaultIotHubClient.newBuilder() //
              .endPoint(HUB_CLOUD_ENDPOINT) //
              .keyStore(KEY_STORE_LOCATION, KEY_STORE_PASSWORD) //
              .alias(ALIAS, ALIAS_PASSWORD) //
              .clientId(solutionId) //
              .apiToken(apiToken);

      if (null != PROXY_URI)
      {
         return propertiesSettable.proxy(URI.create(PROXY_URI)).build();
      }
      else
      {
         return propertiesSettable.build();
      }
   }
}