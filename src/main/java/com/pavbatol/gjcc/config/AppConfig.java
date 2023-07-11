package com.pavbatol.gjcc.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

public class AppConfig {
    private static final Properties properties = new Properties();
    private static final String RESOURCE_APPLICATION_PROPERTIES = "application.properties";
    private static final String PROPERTY_APP_LOG_LEVEL = "app.log.level";
    private static final String SYSTEM_VAR_PROPERTIES_PATH = "properties.path";

    private AppConfig() {
        String propertiesPath = System.getProperty(SYSTEM_VAR_PROPERTIES_PATH);
        try (InputStream inputStream = propertiesPath == null
                ? ClassLoader.getSystemResourceAsStream(RESOURCE_APPLICATION_PROPERTIES)
                : Files.newInputStream(Path.of(propertiesPath))) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + RESOURCE_APPLICATION_PROPERTIES, e);
        }

        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            String systemProperty = System.getProperty(key);
            if (systemProperty != null) {
                properties.setProperty(key, systemProperty);
            }
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

    public String getLogLevel() {
        return getProperty(PROPERTY_APP_LOG_LEVEL);
    }

    private static class InstanceHolder {
        private static final AppConfig INSTANCE = new AppConfig();
    }
}

