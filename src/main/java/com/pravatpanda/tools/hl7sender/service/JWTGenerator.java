package com.pravatpanda.tools.hl7sender.service;

import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by 212791719 (Pravat Panda)on 9/22/2021.
 */
public class JWTGenerator {

    private static final String PKCS8_PRIVATE_KEY_FILE_RSA = "command-center-pkcs8-key.pem";

    public String generateToken(String epicClientId,
                                String tokenUrl) throws Exception {
        System.out.println("===============================================");
        System.out.println("Generating JWT token");

        long now = System.currentTimeMillis();
        String jws = Jwts
                .builder()
                .setHeaderParam("alg", "RS384")
                .setHeaderParam("typ", "JWT")

                .setIssuer(epicClientId)
                .setSubject(epicClientId)
                .setAudience(tokenUrl)
                .setId(UUID.randomUUID().toString())
                .setExpiration(new Date(now + TimeUnit.MINUTES.toMillis(4)))

                .signWith(readPKCS8Key())
                .compact();

        System.out.println("JWT token: \n----------------\n" + jws);
        return jws;
    }

    private static PrivateKey readPKCS8Key() throws Exception {
        StringBuilder pkcs8Lines = new StringBuilder();

        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PKCS8_PRIVATE_KEY_FILE_RSA);
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

    private static PrivateKey readPKCS1Key() throws Exception {
        StringBuilder pkcs8Lines = new StringBuilder();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PKCS8_PRIVATE_KEY_FILE_RSA);
             BufferedReader rdr = new BufferedReader(new InputStreamReader(stream))) {

            String line;
            while ((line = rdr.readLine()) != null) {
                pkcs8Lines.append(line);
            }
        }
        String pkcs1Str = pkcs8Lines.toString();
        // Remove the "BEGIN" and "END" lines, as well as any whitespace
        pkcs1Str = pkcs1Str
                .replaceAll("\\n", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "");


        byte[] bytes = Base64.getDecoder().decode(pkcs1Str);

        DerInputStream derReader = new DerInputStream(bytes);
        DerValue[] seq = derReader.getSequence(0);
        // skip version seq[0];
        BigInteger modulus = seq[1].getBigInteger();
        BigInteger publicExp = seq[2].getBigInteger();
        BigInteger privateExp = seq[3].getBigInteger();
        BigInteger prime1 = seq[4].getBigInteger();
        BigInteger prime2 = seq[5].getBigInteger();
        BigInteger exp1 = seq[6].getBigInteger();
        BigInteger exp2 = seq[7].getBigInteger();
        BigInteger crtCoef = seq[8].getBigInteger();

        RSAPrivateCrtKeySpec keySpec =
                new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        //System.out.println(privateKey);
        return (privateKey);
    }
}
