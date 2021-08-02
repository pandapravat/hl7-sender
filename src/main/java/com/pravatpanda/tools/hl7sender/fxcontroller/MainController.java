/*
 * Copyright (c) 2021. Pravat Panda
 * All rights reserved
 */

package com.pravatpanda.tools.hl7sender.fxcontroller;

import com.pravatpanda.tools.hl7sender.helper.ConfigurationStore;
import com.pravatpanda.tools.hl7sender.service.TLSHl7Sender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

/**
 * Created by 212791719 (Pravat Panda)on 7/22/2021.
 */
public class MainController {
    /**
     * The constant FILE.
     */
    public static final String FILE = System.getProperty("user.home") + "/woa-hl7sender/sender-config.xml";
    /**
     * The constant HOST.
     */
    public static final String HOST = "host";
    /**
     * The constant PORT.
     */
    public static final String PORT = "port";
    /**
     * The constant TLS_CHECKED.
     */
    public static final String TLS_CHECKED = "tlsChecked";
    /**
     * The constant TLS_FILE.
     */
    public static final String TLS_FILE = "tlsFile";
    /**
     * The constant TLS_PASS.
     */
    public static final String TLS_PASS = "tlsPass";
    /**
     * The constant MESSAGE.
     */
    public static final String MESSAGE = "message";
    /**
     * The constant APP_FOOTER_LABEL.
     */
    public static final String APP_FOOTER_LABEL = "app.footer.label";
    public static final String APP_FOOTER_SUBLABEL = "app.footer.sublabel";
    private static final String APP_HEADER_LABEL = "app.header.label";

    /**
     * The Enable tls.
     */
    @FXML CheckBox enableTLS;
    /**
     * The Host field.
     */
    @FXML TextField hostField;
    /**
     * The Port field.
     */
    @FXML TextField portField;
    /**
     * The Trust store file location.
     */
    @FXML TextField trustStoreFileLocation;
    /**
     * The Hl 7 message.
     */
    @FXML TextArea hl7Message;
    /**
     * The Trust store pass.
     */
    @FXML TextField trustStorePass;
    /**
     * The Send button.
     */
    @FXML Button sendButton;
    /**
     * The File chooser.
     */
    @FXML Button fileChooser;
    /**
     * The Response.
     */
    @FXML Label response;
    /**
     * The Footer label.
     */
    @FXML Label footerLabel;
    @FXML Label headerLabel;
    /**
     * The Footer sub label.
     */
    @FXML Label footerSubLabel;
    private Stage stage;
    private ConfigurationStore store;


    /**
     * Initialize.
     */
    @FXML
    public void initialize() {
        footerLabel.setText(ConfigurationStore.getProperty(APP_FOOTER_LABEL));
        footerSubLabel.setText(ConfigurationStore.getProperty(APP_FOOTER_SUBLABEL));
        headerLabel.setText(ConfigurationStore.getProperty(APP_HEADER_LABEL));
        // load last saved configuration
        File file = new File(FILE);
        if(file.exists()) {
            try {
                Properties properties = new Properties();
                properties.loadFromXML(new FileInputStream(file));
                Optional.ofNullable(properties.getProperty(HOST)).ifPresent(hostField::setText);
                Optional.ofNullable(properties.getProperty(PORT)).ifPresent(portField::setText);
                Optional.ofNullable(properties.getProperty(TLS_CHECKED)).map(Boolean::valueOf).ifPresent(enableTLS::setSelected);
                Optional.ofNullable(properties.getProperty(TLS_FILE)).ifPresent(trustStoreFileLocation::setText);
                Optional.ofNullable(properties.getProperty(TLS_PASS)).ifPresent(trustStorePass::setText);
                Optional.ofNullable(properties.getProperty(MESSAGE)).ifPresent(hl7Message::setText);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send message.
     *
     * @param actionEvent the action event
     */
    public void sendMessage(ActionEvent actionEvent) {

        boolean validate = validate();
        if(validate) {
            saveInfo();
            TLSHl7Sender sender = new TLSHl7Sender();
            try {

                boolean tlsSelected = enableTLS.isSelected();

                String ack = sender.send(hostField.getText().trim(),
                        Integer.parseInt(portField.getText().trim()), hl7Message.getText().trim(),
                        tlsSelected, trustStoreFileLocation.getText(), trustStorePass.getText());
                response.setText(ack);
            } catch (Exception e) {
                errorLabel(true);
                String stackTrace = ExceptionUtils.getStackTrace(e);
                response.setText(stackTrace);
            }
        }
    }

    private void saveInfo()  {

        File file = new File(FILE);
        if(file.exists()) {
            FileUtils.deleteQuietly(file);
        } else {
            try {
                file.getParentFile().mkdir();
                file.createNewFile();

                try(FileOutputStream stream = new FileOutputStream(file.getAbsolutePath());) {

                    Properties properties = new Properties();
                    properties.setProperty(HOST, hostField.getText());
                    properties.setProperty(PORT, portField.getText());
                    properties.setProperty(TLS_CHECKED, String.valueOf(enableTLS.isSelected()));
                    properties.setProperty(TLS_FILE, trustStoreFileLocation.getText());
                    properties.setProperty(MESSAGE, hl7Message.getText());

                    properties.setProperty(TLS_PASS, trustStorePass.getText());
                    properties.storeToXML(stream, "Configuration for sender tool");
                } catch (IOException e) {
                    e.printStackTrace();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validate() {
        String host = hostField.getText();
        String port = portField.getText();
        if(StringUtils.isEmpty(host)) {
            errorLabel(true);
            response.setText("Host or port can't be empty");
            return false;
        } else if(StringUtils.isEmpty(port) || !NumberUtils.isDigits(port)) {
            errorLabel(true);
            response.setText("Port should be a number");
            return false;
        }

        String trustFile = trustStoreFileLocation.getText();
        String trustPass = trustStorePass.getText();
        if(enableTLS.isSelected()) {

            File file = new File(trustStoreFileLocation.getText());
            if(StringUtils.isEmpty(trustPass) || StringUtils.isEmpty(trustFile)) {
                errorLabel(true);
                response.setText("Invalid details for TLS. Trust store location and password can't be empty");
                return false;
            } else if(!file.exists()) {
                errorLabel(true);
                response.setText("Trust file does not exist");
                return false;
            } else if(StringUtils.isEmpty(hl7Message.getText())) {
                errorLabel(true);
                response.setText("Message can't be empty");
                return false;
            }
        }

        errorLabel(false);
        return true;
    }

    private void errorLabel(boolean b) {
        if(b) {
            response.setTextFill(Color.RED);
        } else {
            response.setTextFill(Color.BLACK);
        }
    }

    /**
     * Enable tls action.
     *
     * @param actionEvent the action event
     */
    public void enableTLSAction(ActionEvent actionEvent) {
        if(enableTLS.isSelected()) {
            trustStoreFileLocation.setDisable(false);
            trustStorePass.setDisable(false);
            fileChooser.setDisable(false);

        } else {
            trustStoreFileLocation.setDisable(true);
            trustStorePass.setDisable(true);
            fileChooser.setDisable(true);
        }
    }

    /**
     * Choose file.
     *
     * @param actionEvent the action event
     */
    public void chooseFile(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showOpenDialog(stage);
        if(null != file) {
            String absolutePath = file.getAbsolutePath();
            trustStoreFileLocation.setText(absolutePath);
        }
    }

    /**
     * Sets stage.
     *
     * @param primaryStage the primary stage
     */
    public void setStage(Stage primaryStage) {
        this.stage = primaryStage;
    }

    /**
     * Sets config store.
     *
     * @param store the store
     */
    public void setConfigStore(ConfigurationStore store ) {
        this.store = store;
    }

}
