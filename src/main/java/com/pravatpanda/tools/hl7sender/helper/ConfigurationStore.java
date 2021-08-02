/*
 * Copyright (c) 2021. Pravat Panda
 * All rights reserved
 */

package com.pravatpanda.tools.hl7sender.helper;

import java.io.IOException;
import java.util.Properties;

/**
 * The type Configuration store.
 */
public class ConfigurationStore {
    private static Properties properties;
    private ConfigurationStore(){
       //  use static method
    }

    /**
     * Gets property.
     *
     * @param key the key
     * @return the property
     */
    public static String getProperty(String key) {
        if(null == properties) {
            init();
        }
        return properties.getProperty(key);
    }

    /**
     * Init.
     */
    public static void init() {
        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
