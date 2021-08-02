/*
 * Copyright (c) 2021. Pravat Panda
 * All rights reserved
 */

package com.pravatpanda.tools.hl7sender;

import com.pravatpanda.tools.hl7sender.fxcontroller.MainController;
import com.pravatpanda.tools.hl7sender.helper.ConfigurationStore;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by 212791719 (Pravat Panda)on 7/22/2021.
 */
public class MainApp extends Application {

    /**
     * The constant store.
     */
    public static ConfigurationStore store;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.getIcons()
                .addAll(new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream("favicon.ico")));
        FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("sender.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        primaryStage.setTitle(ConfigurationStore.getProperty("app.header.label"));
        primaryStage.setScene(new Scene(root, 700, 600));
        primaryStage.show();
    }


    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException the io exception
     */
    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
