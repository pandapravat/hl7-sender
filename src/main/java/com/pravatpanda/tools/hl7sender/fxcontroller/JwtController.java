package com.pravatpanda.tools.hl7sender.fxcontroller;

import com.pravatpanda.tools.hl7sender.exception.UtilException;
import com.pravatpanda.tools.hl7sender.helper.ConfigurationStore;
import io.jsonwebtoken.Jwts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by 212791719 (Pravat Panda)on 9/22/2021.
 */
public class JwtController {
    @FXML TextField tfAlgorihm;
    @FXML TextField tfType;
    @FXML TextField tfIssuer;
    @FXML TextField tfAudience;
    @FXML TextField tfExpiry;
    @FXML TextField keyFileLocation;
    @FXML
    TextArea taResult;

    public void chooseKeyFile(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        Window window = ((Node) actionEvent.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if(null != file) {
            String absolutePath = file.getAbsolutePath();
            keyFileLocation.setText(absolutePath);
        }
    }

    public void generateToken(ActionEvent actionEvent) throws Exception {
        try {
            validate();

            taResult.setStyle("-fx-text-fill: black ;");
            taResult.setText(getToken());
        } catch (UtilException e) {
            taResult.setStyle("-fx-text-fill: red ;");
            taResult.setText(e.getMessage());
        } catch (Exception e) {
            taResult.setStyle("-fx-text-fill: red ;");
            taResult.setText(ExceptionUtils.getStackTrace(e));
        }
    }

    private void validate() {
        trimAndCheckEmpty(tfAlgorihm, "Algorithm");
        trimAndCheckEmpty(tfType, "Type");
        trimAndCheckEmpty(tfIssuer, "ClientId/Issuer");
        trimAndCheckEmpty(tfAudience, "Token Url");
        trimAndCheckEmpty(tfExpiry, "Expiry");
        if(NumberUtils.isDigits(tfExpiry.getText())) {
            Integer integer = NumberUtils.createInteger(tfExpiry.getText());
            int maxExpiry = Integer.parseInt(ConfigurationStore.getProperty("app.jwt.expirymins"));
            if(integer > maxExpiry) {
                throw new UtilException("Expiry can't be more than " + maxExpiry + " minutes");
            }
        } else {
            throw new UtilException("Expiry should be a number");
        }

        trimAndCheckEmpty(keyFileLocation, "Key file");
    }

    private void trimAndCheckEmpty(TextField textField, String name) {
        String text = textField.getText().trim();
        textField.setText(text);
        if(StringUtils.isEmpty(text)) {
            throw new UtilException(name + " can't be empty");
        }
    }

    private String getToken() throws Exception {
        long now = System.currentTimeMillis();
        return Jwts
                .builder()
                .setHeaderParam("alg", "RS384")
                .setHeaderParam("typ", "JWT")

                .setIssuer(tfIssuer.getText())
                .setSubject(tfIssuer.getText())
                .setAudience(tfAudience.getText())
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(now + TimeUnit.MINUTES.toMillis(4)))

                .signWith(readPKCS8Key())
                .compact();
    }

    private PrivateKey readPKCS8Key() throws Exception {
        StringBuilder pkcs8Lines = new StringBuilder();

        InputStream fileStream = getFileStream();

        try (InputStream stream = fileStream;
             BufferedReader rdr = new BufferedReader(new InputStreamReader(stream));) {

            String line;
            while ((line = rdr.readLine()) != null) {
                pkcs8Lines.append(line);
            }
        }
        String pkcs8LinesStr = pkcs8Lines.toString();
        String key = pkcs8LinesStr
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    private InputStream getFileStream() throws FileNotFoundException {
        String text = keyFileLocation.getText();
        if(text.startsWith("classpath:")) {
            text = StringUtils.substringAfter(text, "classpath:");
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(text);
        } else {
            File file = new File(text);
            if(StringUtils.isEmpty(text) || StringUtils.isEmpty(text)) {
                throw new UtilException("Empty Text");
            } else if(!file.exists()) {
                throw new UtilException("Invalid File Location");
            } else {
                return new FileInputStream(file);
            }
        }
    }

    public void copyTokenToClip(ActionEvent actionEvent) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(taResult.getText());
        clipboard.setContent(content);
    }
}
