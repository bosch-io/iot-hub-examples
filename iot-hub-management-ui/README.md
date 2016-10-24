# Bosch IoT Hub - Example Management User Interface 

This example shows an implementation of a general user interface for the Bosch IoT Hub.

 
# How to run it?

## Create a Solution with a private/public key

https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=020_getting_started:booking

Add your HubClient.jks file to the folder "src/main/resources".

## Configure your Solution Id and other settings

The example management user interface connects two IoT Hub integration clients in order to send and receive messages from the IoT Hub service.
Settings needed for authentication and establishing a connection with the IoT Hub service can be loaded from "config.properties" file located in folder "src/main/resources" 
or can be set in the connection window of this user interface. Please make sure that you have properly configured your solution ID, client IDs and key store settings before starting the example management application.

```
#clientId=### your client id ###
#keystoreLocation=### file:key store location ###
#keystorePassword=### your key store password ###
#keyAlias=### alias name ###
#keyAliasPassword=### your key alias password ###

```

Optionally you will need to define the proxy at the class IoTHubManagementUI.

## Build it

Use the following command to build the Management UI example:

```
mvn clean install
```


## Run it

Use the following command to run the Management UI example:

```
mvn exec:java -Dexec.mainClass="com.bosch.iot.hub.examples.management.ui.IotHubManagementUI"
```


# Features and Limitations

## Features

The example Management UI implements the following features:

* Connectivity management - connect and disconnect to the Bosch IoT Hub service
* Topic management for the IoT Hub - create and delete Topics and apply access control entries for your client ID
* Messaging - send and receive fire-and-forget messages with this application
* Log stream - watch the log entries of all messages arriving from the Bosch IoT Hub service

## Limitations

The example Management UI has the following limitations:

* No support for sending request-response messages
* No support for defining custom message headers
* No support for granting permissions on a Topic towards another solution

## License

See the cr-examples top level README.md file for license details.
