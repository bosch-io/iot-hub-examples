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
package com.bosch.iot.hub.examples.connector.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code HttpDevice} is used for simulating HTTP IoT devices that connect to the Bosch IoT Hub
 * service via HTTP through the example HTTP connector service. {@code HttpDevice} uses Jersey
 * JAX-RS client in order to:
 * <ul>
 * <li>send messages to the IoT Hub service through the HTTP connector service via sending HTTP POST
 * requests against arbitrary URIs denoting the messages topics</li>
 * <li>receive messages form the IoT Hub service through the HTTP connector via consuming
 * server-sent events streamed at arbitrary URIs denoting the message topics</li>
 * </ul>
 * 
 * The HTTP devices use basic authentication to authenticate to the HTTP connector, thus user name
 * and password must be provided as arguments when running {@code HttpDevice} as Java application.
 * In order for the device to be authorized to communicate with the connector service, the user name
 * and SHA-256 password hash must be configured to the HTTP connector's
 * {@code credentials.properties} file.
 * 
 */
public class HttpDevice {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpDevice.class);

	private Client client;
	private List<EventSource> sources = new LinkedList<>();

	/**
	 * Creates new {@link HttpDevice} instance and configures a Jersey JAX-RS client for sending
	 * HTTP PUT requests.
	 * 
	 * @param username username used to authenticate to the HTTP connector using basic
	 *            authentication
	 * @param password password used to authenticate to the HTTP connector using basic
	 *            authentication
	 */
	public HttpDevice(String username, String password) {
		HttpAuthenticationFeature authentication = HttpAuthenticationFeature.basic(username, password);
		client = ClientBuilder.newBuilder().register(authentication).register(SseFeature.class).build();
	}

	/**
	 * Sends a message via sending an HTTP POST request to the HTTP connector service at arbitrary
	 * URIs denoting the message topics. The massage payload, if any, shall be included in the HTTP
	 * request body.
	 * 
	 * @param topic message topic of interest, will be included in the target URI for the request
	 * @param payload optional message payload, can be {@code null}
	 * @param mediaType media type for the message payload
	 */
	public void sendMessage(String topic, String payload, String mediaType) {
		LOGGER.info("Sending POST request at http://localhost:8088/http-connector/messages/{} with body <{}>", topic,
				payload);
		WebTarget target = client.target("http://localhost:8088/http-connector/messages").path(topic);
		try {
			Response response = target.request().post(Entity.entity(payload, mediaType));
			StatusType status = response.getStatusInfo();

			if (status.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
				LOGGER.warn("Error while sending request at http://localhost:8088/http-connector/messages/<{}>: {} {}",
						topic, status.getStatusCode(), status.getReasonPhrase());
			}
		} catch (IllegalArgumentException | ProcessingException e) {
			LOGGER.warn("Error while sending message with topic <{}>", topic, e);
		}
	}

	/**
	 * Starts consuming messages via subscribing for server-sent events streamed by the HTTP
	 * connector service at arbitrary URIs denoting the message topics.
	 * 
	 * @param topic message topic of interest, will be included in the target URI for the request
	 * @param mediaType media type for the message payload
	 */
	public void consumeMessages(String topic, String mediaType) {
		LOGGER.info("Subscribing for server-sent events from source http://localhost:8088/http-connector/messages/{}",
				topic);
		WebTarget target = client.target("http://localhost:8088/http-connector/messages").path(topic);
		EventSource source = EventSource.target(target).build(); // TODO include x-payload-media-type header in HTTP GET request
		source.register(message -> LOGGER.info("Received server-sent event for message at topic <{}> with payload <{}>",
				message.getName(),
				message.readData(String.class, MediaType.valueOf(mediaType))));
		source.open();
		sources.add(source);
	}

	/**
	 * Closes any active server-sent events sources and destroys the JAX-RS client.
	 */
	public void close() {
		LOGGER.info("Closing server-sent events sources and JAX-RS client");
		sources.forEach(source -> source.close());
		client.close();
	}

	/**
	 * Runs a simulated HTTP-connected device. You can use a simple console interface to interact
	 * with the simulated HTTP devices as follows:
	 * <ul>
	 * <li>typing <b>send</b> topic [payload [media type]] will instruct the device to issue an HTTP
	 * POST request and a message to the IoT Hub service through the example HTTP connector</li>
	 * <li>typing <b>consume</b> topic [media type] will instruct the device to subscribe for SSE
	 * and consume messages form the IoT Hub service through the example HTTP connector, consumed
	 * messages will be printed in the console.</li>
	 * </ul>
	 * 
	 * 
	 * @param args arguments used to configure user name and password for the device
	 */
	public static void main(String[] args) {
		//		String username = args[0];
		//		String password = args[1];

		String username = "device2";
		String password = "device2__password!";

		final HttpDevice device = new HttpDevice(username, password);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> device.close()));

		LOGGER.info("Initialized Jersey JAX-RS client with SSE feaure and Basic Authentication enabled.");

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		in.lines().map(line -> line.split("\\s+")).forEach(input -> {
			String command = input[0];
			switch (command) {
			case "send":
				if (input.length >= 2 && input.length <= 4) {
					final String topic = input[1];
					final String payload = getElementAt(input, 2).orElse(null);
					final String mediaType = getElementAt(input, 3).orElse(MediaType.TEXT_PLAIN);
					device.sendMessage(topic, payload, mediaType);
				} else {
					LOGGER.error("Invalid input for command <send> {}", Arrays.asList(input));
				}
				break;

			case "consume":
				if (input.length >= 2 && input.length <= 3) {
					final String topic = input[1];
					final String mediaType = getElementAt(input, 2).orElse(MediaType.TEXT_PLAIN);
					device.consumeMessages(topic, mediaType);
				} else {
					LOGGER.error("Invalid input for command <consume> {}", Arrays.asList(input));
				}
				break;
			}
		});
	}

	private static Optional<String> getElementAt(String[] input, int index) {
		return index > input.length - 1 ? Optional.empty() : Optional.of(input[index]);
	}

}
