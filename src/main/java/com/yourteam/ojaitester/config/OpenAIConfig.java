package com.yourteam.ojaitester.config;

import java.io.InputStream;
import java.util.Properties;

public class OpenAIConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = OpenAIConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found");
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load application.properties", e);
        }
    }

    public static String getApiKey() {
        return properties.getProperty("openai.apiKey");
    }

    public static String getModel() {
        return properties.getProperty("openai.model", "gpt-4.1-mini");
    }
}