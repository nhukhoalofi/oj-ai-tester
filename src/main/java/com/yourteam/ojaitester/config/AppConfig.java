package com.yourteam.ojaitester.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }

            PROPERTIES.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Cannot load application.properties", e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }
}