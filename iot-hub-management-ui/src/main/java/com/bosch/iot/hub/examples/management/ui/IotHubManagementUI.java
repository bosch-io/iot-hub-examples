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
package com.bosch.iot.hub.examples.management.ui;

import com.bosch.iot.hub.client.DefaultIotHubClient;
import com.bosch.iot.hub.client.IotHubClient;
import com.bosch.iot.hub.client.IotHubClientBuilder;
import com.bosch.iot.hub.model.acl.AccessControlList;
import com.bosch.iot.hub.model.acl.AclEntry;
import com.bosch.iot.hub.model.acl.Permission;
import com.bosch.iot.hub.model.acl.Permissions;
import com.bosch.iot.hub.model.message.Message;
import com.bosch.iot.hub.model.message.Payload;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IotHubManagementUI extends Application {


    private static final Logger LOGGER = LoggerFactory.getLogger(IotHubManagementUI.class);
    private static final Logger MESSAGE_LOGGER = LoggerFactory.getLogger("com.bosch.iot.hub.message.log");

    private static final URI BOSCH_IOT_HUB_ENDPOINT_URI = URI.create("wss://hub.apps.bosch-iot-cloud.com");
    private IotHubClient senderIotHubClient = null;
    private IotHubClient receiverIotHubClient;
    private URI privateKeyFile;
    private String clientId;
    private TextArea logStream;
    private TextField clientIdTextField;
    private TextField apiTokenTextField;
    private static Properties props;
    private ObservableList<ACL> data = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) throws Exception {

        Button connectionButton = new Button();
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        props = readProperties();

        primaryStage.getIcons().add(new Image("/hub.png"));

        // File menu - Connect, Exit
        Menu fileMenu = new Menu("File");

        MenuItem connectMenuItem = new MenuItem("Connect");
        connectMenuItem.setOnAction(actionEvent -> {

            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(25, 25, 25, 25));
            Text scenetitle = new Text("Hub Connection Settings");
            scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
            grid.add(scenetitle, 0, 0, 2, 1);
            Label clientIdSetting = new Label("ClientId:");
            grid.add(clientIdSetting, 0, 1);
            clientIdTextField = new TextField();
            clientIdTextField.setText(props.getProperty("clientId"));
            grid.add(clientIdTextField, 1, 1);
            apiTokenTextField = new TextField();
            apiTokenTextField.setText(props.getProperty("apiToken"));
            grid.add(apiTokenTextField, 1, 1);
            Label aliasNameLabel = new Label("Alias name:");
            grid.add(aliasNameLabel, 0, 2);
            TextField aliasNameTextField = new TextField();
            aliasNameTextField.setText(props.getProperty("keyAlias"));
            grid.add(aliasNameTextField, 1, 2);
            Label aliasPasswordLabel = new Label("Alias password:");
            grid.add(aliasPasswordLabel, 0, 3);
            PasswordField aliasPasswordBox = new PasswordField();
            aliasPasswordBox.setText(props.getProperty("keyAliasPassword"));
            grid.add(aliasPasswordBox, 1, 3);
            Label pw = new Label("Private Key password:");
            grid.add(pw, 0, 4);
            PasswordField pwBox = new PasswordField();
            pwBox.setText(props.getProperty("keystorePassword"));
            grid.add(pwBox, 1, 4);
            Label privateKeyLabel = new Label("Private Key:");
            grid.add(privateKeyLabel, 0, 5);
            try {
                if (props.getProperty("keystoreLocation") != null && !props.getProperty("keystoreLocation").isEmpty()) {
                    privateKeyFile = new URI(props.getProperty("keystoreLocation"));
                }
            } catch (URISyntaxException e) {
                LOGGER.warn(e.getMessage());
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Private Key File");
            final Button openButton = new Button("Open Private Key File...");
            openButton.setOnAction(buttonActionEvent -> {
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    privateKeyFile = file.toURI();
                }
            });
            grid.add(openButton, 1, 5);
            final Button connectButton = new Button("Connect to Bosch IoT Hub...");
            connectButton.setOnAction(connectActionEvent -> {
                try {
                    if (!clientIdTextField.getText().isEmpty()) {
                        clientId = clientIdTextField.getText();
                        if (senderIotHubClient == null || !senderIotHubClient.isConnected()) {
                            if (privateKeyFile != null) {
                                senderIotHubClient = createIntegrationClient(clientIdTextField.getText(), privateKeyFile, pwBox.getText(), aliasNameTextField.getText(), aliasPasswordBox.getText(),apiTokenTextField.getText());
                                senderIotHubClient.connect();
                                receiverIotHubClient = createIntegrationClient(clientIdTextField.getText() + ":receiver", privateKeyFile, pwBox.getText(), aliasNameTextField.getText(), aliasPasswordBox.getText(),apiTokenTextField.getText());
                                receiverIotHubClient.connect();
                                receiverIotHubClient.consume(inboundMessage -> MESSAGE_LOGGER.info("Received message for Topic <{}>, sender {} and payload: {}", inboundMessage.getTopicPath().toString(), inboundMessage.getSender().getIdentifier(), inboundMessage.getPayload().get().toString()));
                                LOGGER.info("Creating Hub Integration Client for client ID <{" + clientIdTextField.getText() + "}>.");
                                connectionButton.setStyle("-fx-base: green;-fx-background-color: green;");
                                dialog.close();
                            } else {
                                LOGGER.warn("Some of the required input fields are missing.");
                            }
                        } else {
                            LOGGER.warn("The IoT Hub Client is already connected.");
                        }
                    } else {
                        LOGGER.warn("The clientId field is empty.");
                    }
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Could not establish a connection");

                        alert.setHeaderText(e.getMessage());
                        alert.setContentText(e.toString());
                        alert.showAndWait();
                    });
                    LOGGER.warn("Could not establish a connection: " + e.toString());
                }
            });
            grid.add(connectButton, 0, 6, 2, 1);

            Scene dialogScene = new Scene(grid, 500, 300);
            dialog.setScene(dialogScene);
            dialog.show();
        });
        MenuItem disconnectMenuItem = new MenuItem("Disconnect");
        disconnectMenuItem.setOnAction(actionEvent -> disconnectHubClient(connectionButton));

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> {
            disconnectHubClient(connectionButton);
            Platform.exit();
        });
        fileMenu.getItems().addAll(connectMenuItem, disconnectMenuItem, exitMenuItem);

        Menu helpMenu = new Menu("Help");
        MenuItem gettingStartedMenuItem = new MenuItem("Getting started");
        gettingStartedMenuItem.setOnAction(actionEvent -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://hub.apps.bosch-iot-cloud.com/dokuwiki/doku.php?id=030_dev_guide:examples:hub-ui"));
            } catch (IOException e) {
                LOGGER.warn("Could not open the documentation: " + e.toString());
            }
        });
        helpMenu.getItems().addAll(gettingStartedMenuItem);
        Label connectionStatusLabel = new Label("Connection Status:");
        double r = 7;
        connectionStatusLabel.setFont(new Font(Font.getDefault().getName(), 2 * r));

        connectionButton.setShape(new Circle(r));
        connectionButton.setMinSize(2 * r, 2 * r);
        connectionButton.setMaxSize(2 * r, 2 * r);
        connectionButton.setStyle("-fx-base: red;-fx-background-color: red;");
        HBox connectionHBox = new HBox(
                10,
                connectionStatusLabel,
                connectionButton
        );
        connectionHBox.setAlignment(Pos.CENTER_RIGHT);
        Node topicManagementNode = createTopicManagementTitledPane();
        Node messagingNode = createMessagingTitledPane();
        Node logStreamNode = createLogstreamTitledPane();
        Node messageLogStreamNode = createMessageStreamTitledPane();
        HBox hubfeaturesHBox = new HBox(
                10,
                topicManagementNode,
                messagingNode
        );
        HBox.setHgrow(topicManagementNode, Priority.ALWAYS);
        HBox.setHgrow(messagingNode, Priority.ALWAYS);
        VBox layout = new VBox(
                10,
                menuBar,
                hubfeaturesHBox,
                messageLogStreamNode,
                logStreamNode, connectionHBox
        );
        VBox.setVgrow(topicManagementNode, Priority.ALWAYS);
        VBox.setMargin(connectionHBox, new Insets(5, 5, 10, 10));
        VBox.setMargin(topicManagementNode, new Insets(5, 5, 5, 10));
        VBox.setMargin(messagingNode, new Insets(5, 5, 5, 10));
        VBox.setMargin(logStreamNode, new Insets(5, 5, 5, 10));
        VBox.setMargin(messageLogStreamNode, new Insets(5, 5, 5, 10));
        VBox.setMargin(hubfeaturesHBox, new Insets(5, 5, 5, 10));
        Scene scene = new Scene(layout, 1000, 800, Color.BLACK);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bosch IoT Hub");

        primaryStage.show();
    }

    private void disconnectHubClient(Button connectionButton) {
        if (senderIotHubClient != null && senderIotHubClient.isConnected()) {
            receiverIotHubClient.disconnect();
            receiverIotHubClient.destroy();
            senderIotHubClient.disconnect();
            senderIotHubClient.destroy();
            connectionButton.setStyle("-fx-base: red;-fx-background-color: red;");

        } else {
            LOGGER.warn("The IoT Hub Client is not connected.");
        }
    }

    private static IotHubClient createIntegrationClient(String clientId, URI keyStoreLocation, String keyStorePassword, String aliasName, String aliasPW, String apiToken) throws URISyntaxException {


      /*
       * Provide required configuration (authentication configuration and HUB URI).
       * Proxy configuration is optional and can be added if needed.
       */
        final IotHubClientBuilder.OptionalPropertiesStep builder = DefaultIotHubClient.newBuilder() //
                .endPoint(BOSCH_IOT_HUB_ENDPOINT_URI) //
                .keyStore(keyStoreLocation, keyStorePassword) //
                .alias(aliasName, aliasPW) //
                .clientId(clientId)
                .apiToken(apiToken);
        // .proxy(URI.create("http://" + <proxy-host> + ":" + <proxy port>)); //

        return builder.build();
    }

    private static Properties readProperties() {
        final Properties props = new Properties();

        try {
            InputStream i = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
            props.load(i);
            LOGGER.info("Found a properties file with the following settings: ");
            Enumeration<String> propertyNames = (Enumeration<String>) props.propertyNames();
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                LOGGER.info("Property key: {}, property value: {}", propertyName, props.getProperty(propertyName));
            }

        } catch (IOException e) {
            LOGGER.info(" ==> Config file not found. Please connect your client via the connection window");
        }
        return props;
    }

    private Node createMessageStreamTitledPane() {
        TitledPane messageStreamTitledPane = new TitledPane();
        messageStreamTitledPane.setCollapsible(false);
        messageStreamTitledPane.setText("Message stream");
        messageStreamTitledPane.setPrefSize(1000, 1000);
        TextArea logStream = new TextArea();
        logStream.autosize();

        logStream.setWrapText(true);
        logStream.setEditable(false);
        MessageLogOutputStream outputStream = new MessageLogOutputStream(logStream);

        VBox layout = new VBox(
                10,
                logStream
        );
        VBox.setMargin(logStream, new Insets(5, 5, 5, 10));
        VBox.setVgrow(logStream, Priority.ALWAYS);
        messageStreamTitledPane.setContent(layout);

        LogbackOutputStreamAppender.setStaticOutputStream(outputStream);

        return messageStreamTitledPane;
    }

    private Node createLogstreamTitledPane() {
        TitledPane logStreamTitledPane = new TitledPane();
        logStreamTitledPane.setCollapsible(false);
        logStreamTitledPane.setText("Log stream");
        logStreamTitledPane.setPrefSize(1000, 1000);
        logStream = new TextArea();
        logStream.autosize();

        logStream.setWrapText(true);
        logStream.setEditable(false);
        LogOutputStream outputStream = new LogOutputStream(logStream);
        PrintStream ps = new PrintStream(outputStream, true);
        System.setOut(ps);
        System.setErr(ps);

        final Button clearLogButton = new Button("Clear Logstream");
        clearLogButton.setOnAction(event -> logStream.clear());

        VBox layout = new VBox(
                10,
                logStream,
                clearLogButton
        );
        VBox.setMargin(clearLogButton, new Insets(5, 5, 5, 10));
        VBox.setMargin(logStream, new Insets(5, 5, 5, 10));
        VBox.setVgrow(logStream, Priority.ALWAYS);
        logStreamTitledPane.setContent(layout);
        return logStreamTitledPane;
    }


    private Node createMessagingTitledPane() {
        TitledPane messagingTitledPane = new TitledPane();
        messagingTitledPane.setCollapsible(false);
        messagingTitledPane.setText("Messaging");
        Label topicPathLabel = new Label("Topic:");
        TextField topicPathTextField = new TextField();
        Label messagePayloadLabel = new Label("Message Payload:");
        TextField messagePayloadTextField = new TextField();
        final Button sendMessageButton = new Button("Send Message");
        sendMessageButton.setOnAction(event -> {
            try {
                String payload = "";
                if (messagePayloadTextField.getText() != null && !messagePayloadTextField.getText().isEmpty()) {
                    payload = messagePayloadTextField.getText();
                    senderIotHubClient.send(Message.of(topicPathTextField.getText(), Payload.of(payload))).get(20, TimeUnit.SECONDS);
                } else {
                    senderIotHubClient.send(Message.of(topicPathTextField.getText(), Payload.empty())).get(20, TimeUnit.SECONDS);
                }
                MESSAGE_LOGGER.info("Send Fire-and-Forget Message to Topic with path: <" + topicPathTextField.getText() + "> and payload: " + payload);

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn("Could not send Message to Topic with path: <" + topicPathTextField.getText() + "> " + e.getMessage());
            }
        });

        HBox topicManagementHBox = new HBox(
                10,
                topicPathLabel,
                topicPathTextField
        );
        HBox sendMessageHBox = new HBox(
                10,
                messagePayloadLabel,
                messagePayloadTextField

        );
        VBox layout = new VBox(
                10,
                topicManagementHBox,
                sendMessageHBox,
                sendMessageButton
        );
        VBox.setMargin(sendMessageHBox, new Insets(5, 5, 5, 10));
        VBox.setMargin(topicManagementHBox, new Insets(5, 5, 10, 10));
        VBox.setMargin(sendMessageButton, new Insets(5, 5, 5, 10));
        messagingTitledPane.setContent(layout);
        messagingTitledPane.autosize();
        return messagingTitledPane;
    }

    private Node createTopicManagementTitledPane() {

        TitledPane topicManagementTitledPane = new TitledPane();
        topicManagementTitledPane.setCollapsible(false);
        topicManagementTitledPane.setText("Topic management");
        Label topicPathLabel = new Label("Topic:");
        TextField topicPathTextField = new TextField();
        ComboBox addAclClientIdComboBox = new ComboBox();

        if (props != null && props.getProperty("clientId") != null) {
            addAclClientIdComboBox.getItems().addAll(
                    props.getProperty("clientId") + ":receiver", props.getProperty("clientId"));
        }
        addAclClientIdComboBox.setEditable(true);
        TableView<ACL> table = new TableView<ACL>();
        table.setEditable(true);
        table.setMinHeight(140);
        TableColumn clientIdCol = new TableColumn("ClientId");
        clientIdCol.setCellValueFactory(
                new PropertyValueFactory<ACL, String>("clientId"));
        TableColumn administrateCol = new TableColumn("Administrate");
        administrateCol.setCellValueFactory(
                new PropertyValueFactory<ACL, Boolean>("administrate"));
        TableColumn sendCol = new TableColumn("Send");
        sendCol.setCellValueFactory(
                new PropertyValueFactory<ACL, Boolean>("send"));
        TableColumn receiveCol = new TableColumn("Receive");
        receiveCol.setCellValueFactory(
                new PropertyValueFactory<ACL, Boolean>("receive"));
        table.setItems(data);
        table.getColumns().addAll(clientIdCol, administrateCol, sendCol, receiveCol);

        addAclClientIdComboBox.setPromptText("ClientId");
        addAclClientIdComboBox.setMinWidth(clientIdCol.getPrefWidth());
        final CheckBox senderCheckBox = new CheckBox("Send");
        final CheckBox receiverCheckBox = new CheckBox("Receive");
        final CheckBox administrateCheckBox = new CheckBox("Administrate");

        final Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            if (!addAclClientIdComboBox.getEditor().getText().isEmpty()) {
                for (ACL existingACLEntry : data) {
                    if (existingACLEntry.getClientId().equals(addAclClientIdComboBox.getEditor().getText())) {
                        data.remove(existingACLEntry);
                        break;
                    }
                }
                data.add(new ACL(
                        addAclClientIdComboBox.getEditor().getText(),
                        senderCheckBox.isSelected(),
                        receiverCheckBox.isSelected(),
                        administrateCheckBox.isSelected()));
                addAclClientIdComboBox.getSelectionModel().clearSelection();
            } else {
                LOGGER.info("Can not add ACL entry. No clientId is specified");
            }
        });
        table.setRowFactory(tableView -> {
            final TableRow<ACL> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem removeMenuItem = new MenuItem("Remove");
            removeMenuItem.setOnAction(event -> table.getItems().remove(row.getItem()));
            contextMenu.getItems().add(removeMenuItem);
            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
        HBox rateLayout = new HBox(10, administrateCheckBox, senderCheckBox, receiverCheckBox);
        HBox addACLLayoutBox = new HBox(10, addAclClientIdComboBox, administrateCheckBox, senderCheckBox, receiverCheckBox, addButton);
        rateLayout.setAlignment(Pos.CENTER_LEFT);
        final Button createTopicButton = new Button("Create Topic");
        createTopicButton.setOnAction(event -> {
            try {
                if (topicPathTextField.getText() != null && !topicPathTextField.getText().isEmpty()) {
                    if (senderIotHubClient != null) {
                        java.util.List<AclEntry> list = new ArrayList<>();
                        for (ACL currentACLEntry : data) {
                            AclEntry aclEntry = null;
                            if (currentACLEntry.isAdministrate()) {
                                if (currentACLEntry.isReceive()) {
                                    if (currentACLEntry.isSend()) {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.all());
                                    } else {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.ADMINISTRATE, Permission.RECEIVE));
                                    }
                                } else {
                                    if (currentACLEntry.isSend()) {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.ADMINISTRATE, Permission.SEND));

                                    } else {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.ADMINISTRATE));
                                    }

                                }
                            } else {
                                if (currentACLEntry.isReceive()) {
                                    if (currentACLEntry.isSend()) {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.RECEIVE, Permission.SEND));
                                    } else {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.RECEIVE));
                                    }
                                } else {
                                    if (currentACLEntry.isSend()) {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.of(Permission.SEND));

                                    } else {
                                        aclEntry = AclEntry.of(currentACLEntry.getClientId(), Permissions.none());
                                    }

                                }
                            }
                            list.add(aclEntry);

                        }
                        if (list.isEmpty()) {
                            LOGGER.info("Can not create a Topic. NO ACL entry is specified.");

                        } else {
                            senderIotHubClient.createTopic(topicPathTextField.getText(), AccessControlList.of(list)).whenComplete((topic, e) -> {
                                if (e == null) {
                                    LOGGER.info("Successfully created Topic with  path <{}> and ACL {} .", topic.getPath(), topic.getAcl());
                                }
                            }).get(30, TimeUnit.SECONDS);
                        }


                    } else {
                        LOGGER.warn("Please connect the IoT Hub Client.");
                    }
                } else {
                    LOGGER.warn("Could not create a Topic with an empty String");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException | IllegalStateException e) {
                LOGGER.warn("Could not create Topic with path: <" + topicPathTextField.getText() + "> " + e.getMessage());
            }
        });
        final Button deleteTopicButton = new Button("Delete Topic");
        deleteTopicButton.setOnAction(event -> {
            try {
                if (topicPathTextField.getText() != null && !topicPathTextField.getText().isEmpty()) {
                    if (senderIotHubClient != null) {
                        senderIotHubClient.deleteTopic(topicPathTextField.getText()).whenComplete((Void, e) -> {
                            if (e == null) {
                                LOGGER.info("Successfully deleted Topic with  path <{}>.", topicPathTextField.getText());
                            }
                        }).get(30, TimeUnit.SECONDS);
                    } else {
                        LOGGER.warn("Please connect the IoT Hub Client.");
                    }
                } else {
                    LOGGER.warn("Could not delete a Topic with an empty String");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.warn("Could not delete Topic with path: <" + topicPathTextField.getText() + "> " + e.getMessage());
            }
        });

        HBox buttonControlHBox = new HBox(
                10,
                createTopicButton,
                deleteTopicButton
        );
        HBox topicManagementHBox = new HBox(
                10,
                topicPathLabel,
                topicPathTextField

        );

        final VBox aclControlVBox = new VBox();
        aclControlVBox.setSpacing(5);
        aclControlVBox.setPadding(new Insets(10, 0, 0, 10));
        aclControlVBox.getChildren().addAll(table, addACLLayoutBox);

        VBox layout = new VBox(
                10,
                topicManagementHBox,
                aclControlVBox,
                buttonControlHBox
        );
        VBox.setMargin(buttonControlHBox, new Insets(5, 5, 12, 10));
        VBox.setMargin(topicManagementHBox, new Insets(5, 5, 10, 10));
        VBox.setMargin(aclControlVBox, new Insets(5, 5, 5, 10));
        VBox.setMargin(addACLLayoutBox, new Insets(5, 5, 5, 10));
        topicManagementTitledPane.setContent(layout);
        topicManagementTitledPane.autosize();

        return topicManagementTitledPane;

    }

    public static void main(String[] args) {
        launch(args);
    }
}