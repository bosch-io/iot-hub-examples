# Bosch IoT Hub - Example HTTP Connector

This example shows an implementation of a general purpose HTTP Connector for the Bosch IoT Hub.

# How does it work?

## Overview

The following diagram shows how the Example HTTP Connector could work:

![overview](overview.png)


 
# How to run it?

## Create a Solution with a private/public key

https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking

Add the HubClient.jks to the folder "src/main/resources".

## Configure your Solution Id and other settings

The example HTTP Connector incorporates two IoT Hub integration clients in order to send and receive messages from the IoT Hub service.
Settings needed for authentication and establishing a connection with the IoT Hub service are loaded from "config.properties" file located in folder "src/main/resources".
Please make sure that you have properly configured your solution id, client ids and key store settings before starting the HTTP Connector application.

```
iotHubEndpoint=wss\://hub.apps.bosch-iot-cloud.com
solutionId=###your solution id###
senderId=###id of the client used for sending messages to the hub service, format "<solutionId>:http-connector-sender"###
consumerId=###id of the client used for consuming messages from the hub service, format "<solutionId>:http-connector-consumer"###
keystoreLocation=HubClient.jks
keystorePassword=### your key store password###
keyAlias=Hub
keyAliasPassword=### your key alias password###

#httpProxyHost=### your http proxy host, if you need one ###
#httpProxyPort=### your http proxy port, if you need one ###
#httpProxyPrincipal=### the principle to be used for authentication at the http proxy ###
#httpProxyPassword=### the password to be used for authentication at the http proxy ###
```

## Authorize your HTTP devices to communicate with the HTTP Connector

The example HTTP Connector employs basic authentication for HTTP-connected devices.
Credentials for devices which shall be authorized to communicate with the HTTP Connector are loaded form "credentials.properties" file located in folder "src/main/resources", passwords should be SHA-256 hash-encoded.
Please make sure that you have properly configured the list of authorized devices before starting the HTTP Connector application.

```
#password for "device1" is sha-256 hash of "device1__passowrd!"
#password for "device2" is sha-256 hash of "device2__password!"
#password for "device3" is sha-256 hash of "device3__password!"
device1=31c083858857360580dc212f7487fc317dd8a4f436e489d693f24c1dadbbad93
device2=e8c56ccacf97ea5d88da7f0b7713f2ccf6afa9a71935554ba5ae203b481d4cf6
device3=8518a9257f0a485db573ce852fd5bfecc7efd2eb2cb53b8dfe43fb496d469662
```

## Configure ACL for "HTTP Connector" to your Topics

Configure the ACL for each of your Topics accordingly so that the IoT Hub integration clients incorporated in the HTTP Connector (sender and consumer) are authorized to send and receive messages based on your integration scenario.

```
[
    {
       "authSubject": "### your solution id ###:http-connector-sender"
       "permissions": ["SEND", "RECEIVE"]
    },
    {
       "authSubject: "### your solution id ###:http-connector-consumer",
       "permissions": ["RECEIVE"]
    }
 ]
```

## Build it

Use the following command to build the HTTP Connector example:

```
mvn clean install
```


## Run it

Use the following command to run the HTTP Connector example:

```
mvn exec:java -Dexec.mainClass="com.bosch.iot.hub.examples.connector.http.HttpConnectorApplication"
```


## Usage

### Sending messages through HTTP Connector service

Authenticated HTTP devices can send messages to the IoT Hub service by initiating an HTTP POST requests at [http://localhost:8088/http-connector/messages/**].
The message topic is derived from the HTTP request URI and the message payload is extracted from the HTTP request body.

You can send messages at your Topics through the HTTP connector by using your browser's REST client or cURL.
You need to authenticate (basic authentication) as one of the authorized devices which credentials are configured in the "credentials.properties" file.
For example, the following HTTP request will send a message at topic "com.example.http/devices/commands/modify/device1"

```
POST /http-connector/messages/com.example.http/devices/commands/modify/device1 HTTP/1.1
Content-Type: application/json
Authorization: Basic ZGV2aWNlMTpkZXZpY2UxX19wYXNzd29yZCE=
{	
	"attribute":"counter",
	"value":54
}

HTTP/1.1 201 Created
Location: http://localhost:8080/http-connector/messages/com.example.http/devices/commands/modify/device1

```

### Consuming messages through HTTP Connector service

Authenticated HTTP devices can consume messages form the IoT Hub service by subscribing for server-sent events streamed at [http://localhost:8088/http-connector/messages/**].
The topic of interest is derived from the HTTP request URI, each event in the stream contains "name" field - specifying the message topic, and one or more "data" fields - carrying the message payload.
The media type of the event "data" may be defined by including "x-payload-media-type" header in the HTTP request, "text/plain" will be assumed by default.
Please note that '\n' and '\r' characters are delimiters for the various fields in the event stream, thus, a paylod that contains such characters would be split into multiple "data" fields in the stream.

You can receive messages at your Topics through the HTTP connector by using a SSE enabled HTTP client like cURL or similar. 
You need to authenticate (basic authentication) as one of the authorized devices which credentials are configured in the "credentials.properties" file.
For example, the following HTTP request will consume SSE for messages at topic "com.example.http/devices/commands/modify/device1"

```
GET /http-connector/messages/com.example.http/devices/commands/modify/device1 HTTP/1.1
Accept: text/event-stream
Authorization: Basic ZGV2aWNlMTpkZXZpY2UxX19wYXNzd29yZCE=

HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache
Connection: keep-alive

name: com.example.http/devices/commands/modify/device1
data: {	
data: 	"attribute":"counter",
data: 	"value":53
data: }

name: com.example.http/devices/commands/modify/device1
data: {	
data: 	"attribute":"counter",
data: 	"value":54
data: }


name: com.example.http/devices/commands/modify/device1
data: {"attribute":"counter","value":55}

```

If your browser supports server-sent events, you can also use the provided SSE Log view available at [http://localhost:8088/http-connector/messagelog/**] to subscribe for and receive SSE.
For example, opening [http://localhost:8088/http-connector/messagelog/com.example.http/devices/commands/modify/device1] will subscribe for SSE streamed at [http://localhost:8088/http-connector/messages/com.example.http/devices/commands/modify/device1] and display the SSE log for messages at topic "com.example.http/devices/commands/modify/device1"

![messagelog](messagelog.png)


### Run and control a simulated HTTP-connected device with Java

You can also simulate an HTTP-connected IoT device by running the `HttpDevice.java` located in "src/test/java" as Java application.
You can use a simple console interface to interact with the simulated HTTP devices as follows:
* typing **send** ### topic ### [### payload ### [ ### media type ###]] will instruct the device to issue an HTTP POST request and a message to the IoT Hub service through the example HTTP connector
* typing **consume** ### topic ### [ ### media type ### ] will instruct the device to subscribe for SSE and consume messages form the IoT Hub service through the example HTTP connector, consumed messages will be printed in the console.


```
consume com.example.http/devices/commands/modify/device1
2016-06-16 18:42:55.067  INFO   --- [           main] c.b.i.h.e.connector.http.HttpDevice      : Subscribing for server-sent events from source http://localhost:8088/http-connector/messages/com.example.http/devices/commands/modify/device1
2016-06-16 18:43:10.736  INFO   --- [dify/device1]-0] c.b.i.h.e.connector.http.HttpDevice      : Received server-sent event for message at topic <com.example.http/devices/commands/modify/device1> with payload <{"attribute":"counter","value":53}>
2016-06-16 18:44:13.832  INFO   --- [dify/device1]-0] c.b.i.h.e.connector.http.HttpDevice      : Received server-sent event for message at topic <com.example.http/devices/commands/modify/device1> with payload <{"attribute":"counter","value":54}>


send com.example.http/devices/commands/modify/device1 {"attribute":"counter","value":55} application/json
2016-06-16 18:45:50.855  INFO   --- [           main] c.b.i.h.e.connector.http.HttpDevice      : Sending POST request at http://localhost:8088/http-connector/messages/com.example.http/devices/commands/modify/device1 with body <{"attribute":"counter","value":55}>

```

# Features and Limitations

## Features

The example HTTP Connector service implements the following features:

* Basic authentication of HTTP-connected devices based on a configurable list of credential hashes managed by the HTTP Connector
* Topic authorization based on IoT Hub authorization model
* Sending of fire-and-forget messages from authenticated HTTP-connected devices to the IoT Hub service via accepting HTTP POST requests against arbitrary URIs denoting the messages topics
* Delivering of fire-and-forget messages form the IoT Hub service to authenticated HTTP-connected devices via server-sent events (SSE) streamed at arbitrary URIs denoting the message topics


## Limitations

The example HTTP Connector service has the following limitations:

* No support for exchanging messages using the request-response messaging pattern, only fire-and-forget messages are supported
* No support for mapping custom message headers from/to the HTTP context, only message topic and payload are mapped over HTTP
* No support for buffering messages at the HTTP Connector, received messages are delivered only to the currently connected HTTP devices
* No support for per-device message dispatching, no support for defining and handling fine-grained device-related permissions on Topics


## License

See the cr-examples top level README.md file for license details.
