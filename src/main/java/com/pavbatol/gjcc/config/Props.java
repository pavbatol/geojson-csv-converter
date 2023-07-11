package com.pavbatol.gjcc.config;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * The class contains the names (keys) of variables that can be located in application.properties,
 * as well as can be used as system variables, for example, to transfer them when starting a JAR file.
 */
@RequiredArgsConstructor
public enum Props {
    LOG_LEVEL_ROOT("app.log.level.root"),
    LOG_LEVEL_COM_PAVBATOL("app.log.level.com.pavbatol"),
    DATA_PRESET_FILE_PATHS("app.data.file-path"),
    DATA_DIRECTORY_OUTPUT("app.data.directory.output"),
    DATA_DIRECTORY_INPUT_GENERATED("app.data.directory.input.generated"),
    DATA_DIRECTORY_INPUT_DEFAULT("app.data.directory.input.default");

    private final String key;
    private static final Set<String> keys = new HashSet<>();

    static {
        for (Props props : Props.values()) {
            if (!keys.add(props.key)) {
                throw new IllegalArgumentException("Duplicate key: " + props.key);
            }
        }
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return AppConfig.getInstance().getProperty(key);
    }
}
