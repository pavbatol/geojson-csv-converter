package com.pavbatol.gjcc.config;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * The class contains the names (keys) of variables that can be used as system variables,
 * for example, to transfer them when starting a JAR file, as well as keys from Props.
 * But unlike Props enum, these keys are not considered as local resources that can be located in application.properties
 */
@RequiredArgsConstructor
public enum SystemProps {
    PROPERTIES_PATH("properties.path"),
    ;

    private final String key;
    private static final Set<String> keys = new HashSet<>();

    static {
        for (SystemProps systemProps : SystemProps.values()) {
            if (!keys.add(systemProps.key)) {
                throw new IllegalArgumentException("Duplicate key: " + systemProps.key);
            }
        }
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return System.getProperty(key);
    }
}
