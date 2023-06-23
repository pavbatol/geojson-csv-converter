package com.pavbatol.gjcc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();
    public static final String APPLICATION_PROPERTIES = "application.properties";

    private AppConfig() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(APPLICATION_PROPERTIES)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + APPLICATION_PROPERTIES, e);
        }
    }

    public static AppConfig getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Properties getProperty() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private static class InstanceHolder {
        private static final AppConfig INSTANCE = new AppConfig();
    }
}

