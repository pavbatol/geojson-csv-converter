package com.pavbatol.gjcc.returns.impl;

import com.pavbatol.gjcc.returns.ReturnStatus;
import com.pavbatol.gjcc.returns.abstracts.AbstractReturnData;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ReturnArrayData extends AbstractReturnData {
    String[] values;

    public ReturnArrayData(ReturnStatus status, String[] values) {
        super(status);
        this.values = values;
    }

    public static ReturnArrayData of(ReturnStatus status) {
        return new ReturnArrayData(status, null);
    }
}
