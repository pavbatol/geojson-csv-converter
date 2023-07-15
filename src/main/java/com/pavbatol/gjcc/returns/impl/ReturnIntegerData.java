package com.pavbatol.gjcc.returns.impl;

import com.pavbatol.gjcc.returns.ReturnStatus;
import com.pavbatol.gjcc.returns.abstracts.AbstractReturnData;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ReturnIntegerData extends AbstractReturnData {
    Integer value;

    public ReturnIntegerData(ReturnStatus status, Integer value) {
        super(status);
        this.value = value;
    }

    public static ReturnIntegerData of(ReturnStatus status) {
        return new ReturnIntegerData(status, null);
    }
}
