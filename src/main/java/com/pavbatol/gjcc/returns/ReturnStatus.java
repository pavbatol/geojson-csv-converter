package com.pavbatol.gjcc.returns;

public enum ReturnStatus {
    STOP,
    RESET,
    OK;

    private static final int MAX_ELEMENTS = 3;

    static {
        if (values().length > MAX_ELEMENTS) {
            throw new IllegalStateException("ReturnStatus enum cannot have more than " + MAX_ELEMENTS + " elements.");
        }
    }
}
