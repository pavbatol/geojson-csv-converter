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
    LOG_LEVEL_ROOT("log.level.root"),
    LOG_LEVEL_COM_PAVBATOL("log.level.com.pavbatol"),
    DATA_PRESET_FILE_PATHS("data.preset.file-paths"),
    DATA_DIRECTORY_OUTPUT("data.directory.output"),
    DATA_DIRECTORY_INPUT_GENERATED("data.directory.input.generated"),
    DATA_DIRECTORY_INPUT_DEFAULT("data.directory.input.default");

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
