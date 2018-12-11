# Command & Control Application

The application shows how to connect to the IoT Hub messaging endpoint and send a command to a device in one-way mode.
It sends automatically a "switchLight" command with the payload "On" to a device which must be connected.

For a more complex example see the [Hono example application](https://github.com/eclipse/hono/tree/master/example)
The guide only works for Linux & Mac systems, for MS Windows you have to adapt the paths and options.

# Prerequisites  

following software must be installed:

* Java 8 (Oracle JDK or OpenJDK) or newer
* maven 3.5.2 or newer
* [mosquitto_sub](https://mosquitto.org/) for subscribing to receive commands


An existing IoT Hub service instance and a valid tenant ID are needed to run the application. 
Please consult [IoT Hub documentation](https://docs.bosch-iot-hub.com/booktenant.html) on how to book a IoT Hub service instance. 
In the 'Credentials' information of a IoT Hub service subscription information, you will find the 'tenant-id', the 'messaging-username' and 'messaging-password'. 
Please register a device and setup password credentials. You will need the 'device-id', 'auth-id' and the matching secret for the 'auth-id'.  

## Subscribe to receive commands

To receive commands, a device must subscribe to a specific control topic. 
As exemplary device the Eclipse Mosquitto MQTT client is used. Please make sure auth-id and the matching secret is set correctly.
  
~~~
mosquitto_sub -d -h mqtt.bosch-iot-hub.com -p 8883 -u {auth-id}@{tenant-id} -P {secret} --capath /etc/ssl/certs/ -t control/+/+/req/#
~~~

## Send commands 

To build and package the application as a runnable JAR file run:

~~~
mvn clean package -DskipTests
~~~

The command application needs a few parameters set to run. Please make sure the following are set correctly:

* `messaging-username`: the username for the IoT Hub messaging endpoint (messaging@tenant-id)
* `messaging-password`: the password for the IoT Hub messaging endpoint
* `tenant-id`: the tenant ID
* `device-id`: a device ID to send command


To start the application, run:

~~~
java -jar target/command-and-control-1.0-SNAPSHOT.jar --hono.client.tlsEnabled=true --hono.client.username={messaging-username} --hono.client.password={messaging-password} --tenant.id={tenant-id} --device.id={device-id}
~~~

The application will connect to the IoT Hub and send the command to a subscribed device. You should see the received command in the 'mosquitto_sub' output. 