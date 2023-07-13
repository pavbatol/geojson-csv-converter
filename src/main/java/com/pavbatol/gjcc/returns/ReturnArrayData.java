package com.pavbatol.gjcc.returns;

import lombok.NonNull;
import lombok.Value;

@Value
public class ReturnArrayData {
    @NonNull ReturnStatus status;
    String[] values;

    public static ReturnArrayData of(ReturnStatus status) {
        return new ReturnArrayData(status, null);
    }
}
