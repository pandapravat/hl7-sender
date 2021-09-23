package com.pravatpanda.tools.hl7sender.fxcontroller;

import com.pravatpanda.tools.hl7sender.helper.ConfigurationStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Created by 212791719 (Pravat Panda)on 9/22/2021.
 */
public class FooterController {
    public static final String APP_FOOTER_LABEL = "app.footer.label";
    public static final String APP_FOOTER_SUBLABEL = "app.footer.sublabel";

    @FXML
    Label footerSubLabel;
    @FXML Label footerLabel;
    @FXML
    public void initialize() {
        footerLabel.setText(ConfigurationStore.getProperty(APP_FOOTER_LABEL));
        footerSubLabel.setText(ConfigurationStore.getProperty(APP_FOOTER_SUBLABEL));
    }
}
