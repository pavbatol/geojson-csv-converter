package com.pavbatol.gjcc.converter;

import lombok.Value;

@Value
public class ReturnIntegerData {
    ReturnStatus status;
    Integer value;

    public static ReturnIntegerData of(ReturnStatus status) {
        return new ReturnIntegerData(status, null);
    }
}
