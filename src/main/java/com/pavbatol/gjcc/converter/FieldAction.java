package com.pavbatol.gjcc.converter;

import lombok.Getter;

public enum FieldAction {
    SKIP_FIELD,
    AS_IS_NAME,
    CUSTOM_NAME;

    @Getter
    private String name;

    public FieldAction setName(String name) {
        this.name = name;
        return this;
    }
}
