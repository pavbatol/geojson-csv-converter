package com.pavbatol.gjcc.converter;

import lombok.Value;

@Value
public class ReturnArrayData {
    ReturnStatus status;
    String[] values;

    public static ReturnArrayData of(ReturnStatus status) {
        return new ReturnArrayData(status, null);
    }
}
