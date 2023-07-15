package com.pavbatol.gjcc.returns.impl;

import com.pavbatol.gjcc.field.FieldAction;
import com.pavbatol.gjcc.returns.ReturnStatus;
import com.pavbatol.gjcc.returns.abstracts.AbstractReturnData;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ReturnDetectedFieldData extends AbstractReturnData {
    FieldAction fieldAction;
    Boolean skipRemainingFields;
    Boolean loadRemainingFields;

    public ReturnDetectedFieldData(ReturnStatus status,
                                   FieldAction fieldAction,
                                   Boolean skipRemainingFields,
                                   Boolean loadRemainingFields) {
        super(status);
        this.fieldAction = fieldAction;
        this.skipRemainingFields = skipRemainingFields;
        this.loadRemainingFields = loadRemainingFields;
    }

    public static ReturnDetectedFieldData of(ReturnStatus status) {
        return new ReturnDetectedFieldData(status, null, null, null);
    }
}
