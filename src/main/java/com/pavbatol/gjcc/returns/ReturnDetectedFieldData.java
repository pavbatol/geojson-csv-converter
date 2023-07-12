package com.pavbatol.gjcc.returns;

import com.pavbatol.gjcc.field.FieldAction;
import lombok.Value;

@Value
public class ReturnDetectedFieldData {
    ReturnStatus status;
    FieldAction fieldAction;
    Boolean skipRemainingFields;
    Boolean loadRemainingFields;

    public static ReturnDetectedFieldData of(ReturnStatus satus) {
        return new ReturnDetectedFieldData(satus, null, null, null);
    }
}
