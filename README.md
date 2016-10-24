# Examples for the Bosch IoT Hub cloud service

[![Join the chat at https://gitter.im/bsinno/cr-examples](https://badges.gitter.im/bsinno/cr-examples.svg)](https://gitter.im/bsinno/cr-examples?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This repository contains examples for using the Bosch IoT Hub in the cloud.

## Preparation

### Maven Repository
 
In order to be able to run the examples (or to implement your own), you need the "Bosch IoT Hub Client" (short: *HIC*).
This is available via our public Maven repository. Add following Maven-Repository to your Maven `settings.xml`:

```
   ..
   <repositories>
      <repository>
         <id>bosch-releases</id>
         <url>https://maven.bosch-si.com/content/repositories/bosch-releases/</url>
         <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
         </releases>
         <snapshots>
            <enabled>false</enabled>
            <updatePolicy>daily</updatePolicy>
         </snapshots>
      </repository>
      ..
   </repositories>
   ..
```
### Maven Depedency

After adding the public repository as described above, you can simply use the HIC dependency to your `pom.xml`:

```
  <dependency>
      <groupId>com.bosch.iot.hub</groupId>
      <artifactId>iot-hub-client</artifactId>
      <version>${hic.version}</version>
   </dependency>
```

## License

The examples are made available under the terms of Bosch SI Example Code License. See individual files for details.
