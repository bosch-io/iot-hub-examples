package com.bosch.iot.hub.examples.connector.http;

import com.bosch.iot.hub.client.DefaultIotHubClient;
import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.IotHubClientBuilder;
import com.bosch.iot.hub.model.acl.AccessControlList;
import com.bosch.iot.hub.model.acl.AclEntry;
import com.bosch.iot.hub.model.acl.Permission;
import com.bosch.iot.hub.model.acl.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Topic management example which can be used for:
 * <ul>
 * <li>creating the entry Topic with sender and receiver permissions</li>
 * </ul>
 * <p>
 */
public class DeleteTopic {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDevice.class);
    private static String solutionId;
    private static IotHubClient iotHubClient;
    private static String entryTopic = "<your entry Topic>";

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

        LOGGER.info("Initialized Topics for the Bosch IoT Hub.");

        // load the configuration for the Topic management
        Properties configuration = new Properties(System.getProperties());
        try {
            configuration.load(HttpConnectorController.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            LOGGER.warn("Error while loading IoT Hub integration cleint configuration");
            throw new RuntimeException(e);
        }

        LOGGER.info("Loaded IoT Hub integration client configuration : {}", configuration);

        solutionId = configuration.getProperty("solutionId");
        String senderId = configuration.getProperty("senderId", solutionId + ":http-connector-sender");
        String consumerId = configuration.getProperty("consumerId", solutionId + ":http-connector-consumer");
        // create the client to manage the Topic's
        iotHubClient = constructIotHubClient(senderId, configuration);

        final AclEntry expectedEntry1 = AclEntry.of(senderId, Permissions.all());
        final AclEntry expectedEntry2 = AclEntry.of(consumerId, Permission.RECEIVE);
        final AccessControlList acl = AccessControlList.of(expectedEntry1, expectedEntry2);

        iotHubClient.deleteTopic(solutionId).thenAccept(Void -> {
            LOGGER.info("Delete topic with path : <{}>", solutionId);
        }).get(10, TimeUnit.SECONDS);
        iotHubClient.destroy();

    }

    /**
     * Constructs a new {@link IotHubClient} instance using the provided configuration and client id, establishes a
     * connection to the Bosch IoT Hub service.
     *
     * @param clientId      client identifier
     * @param configuration properties providing IoT Hub endpoint URI, client authentication configuration and optionally
     *                      HTTP proxy settings
     * @return new {@link IotHubClient} instance
     */
    private static IotHubClient constructIotHubClient(String clientId, Properties configuration) {
        LOGGER.info("Constructing IoT Hub integration client for client ID <{}>.", clientId);

        // endpoint of the bosch iot cloud service
        URI iotHubEndpoint =
                URI.create(configuration.getProperty("iotHubEndpoint", "wss://hub.apps.bosch-iot-cloud.com"));

        // location and password for the key store holding your private key
        // client authentication is implemented by a handshake using an asymmetric key-pair
        String keystoreLocation = configuration.getProperty("keystoreLocation", "HubClient.jks");
        String keystorePassword = configuration.getProperty("keystorePassword");

        // key alias and password
        String keyAlias = configuration.getProperty("keyAlias", "Hub");
        String keyAliasPassword = configuration.getProperty("keyAliasPassword");
        String apiToken = configuration.getProperty("solutionApiToken");

        try {
            // provide required configuration (authentication configuration and iot hub endpoint)
            // proxy configuration is optional and can be added if the proxy configuration properties exist
           URI keystoreUri = Thread.currentThread().getContextClassLoader().getResource(keystoreLocation).toURI();
           final IotHubClientBuilder.OptionalPropertiesStep builder = DefaultIotHubClient.newBuilder() //
              .endPoint(iotHubEndpoint) //
              .keyStore(keystoreUri,keystorePassword) //
              .alias(keyAlias, keyAliasPassword) //
              .clientId(clientId) //
              .apiToken(apiToken);

            // http proxy settings, optional
            String httpProxyHost = configuration.getProperty("httpProxyHost");
            String httpProxyPort = configuration.getProperty("httpProxyPort");
            String httpProxyPrincipal = configuration.getProperty("httpProxyPrincipal");
            String httpProxyPassword = configuration.getProperty("httpProxyPassword");

            // configure http proxy for the client, if provided
            if (httpProxyHost != null && httpProxyPort != null) {
                IotHubClientBuilder.OptionalProxyPropertiesStep proxy =
                        builder.proxy(URI.create("http://" + httpProxyHost + ':' + httpProxyPort));

                if (httpProxyPrincipal != null && httpProxyPassword != null) {
                    proxy.proxyAuthentication(httpProxyPrincipal, httpProxyPassword);
                }
            }

            IotHubClient client = builder.build(); // build the client
            client.connect(); // establish a connection with the iot hub service

            return client;

        } catch (URISyntaxException e) {
            LOGGER.info("Error while constructing IoT Hub integration client for client ID <{}>.", clientId);
            throw new RuntimeException(e);
        }
    }
}
