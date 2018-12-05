# Example Consumer Application

The example consumer application shows how to connect Hono client to the IoT Hub messaging (consumer) endpoint and create a telemetry consumer in order to consume telemetry messages. An existing IoT Hub service instance and a valid tenant ID are needed to run the example application. Please consult [IoT Hub documentation](https://docs.bosch-iot-hub.com/booktenant.html) on how to book a IoT Hub service instance.

## Build and Package

To build and package the application as a runnable JAR file run:

~~~
mvn clean package -DskipTests
~~~

## Run Example Consumer Application

The example consumer application needs a few parameters set to run. Please make sure the following are set correctly:

* `messaging-username`: the username for the IoT Hub messaging endpoint (messaging@tenant-id)
* `messaging-password`: the password for the IoT Hub messaging endpoint
* `tenant-id`: the tenant ID

All the information needed for setting these parameters can be found in the 'Credentials' information of a IoT Hub service subscription information.

To start the example consumer application (Linux & Mac), run:

~~~
java -jar example-consumer/target/example-consumer-1.0-SNAPSHOT.jar --hono.client.tlsEnabled=true --hono.client.username={messaging-username} --hono.client.password={messaging-password} --tenant.id={tenant-id}
~~~

The consumer application is ready as soon as 'Consumer ready' is printed on the console. The startup can take up to 10 seconds.