package com.pavbatol.gjcc.returns.impl;

import com.pavbatol.gjcc.returns.ReturnStatus;
import com.pavbatol.gjcc.returns.abstracts.AbstractReturnData;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class ReturnLoadingFieldsWayData extends AbstractReturnData {
    Boolean allFields;
    Boolean specifiedFields;
    String[] inputFields;

    public ReturnLoadingFieldsWayData(ReturnStatus status,
                                      Boolean allFields,
                                      Boolean specifiedFields,
                                      String[] inputFields) {
        super(status);
        this.allFields = allFields;
        this.specifiedFields = specifiedFields;
        this.inputFields = inputFields;
    }

    public static ReturnLoadingFieldsWayData of(ReturnStatus returnStatus) {
        return new ReturnLoadingFieldsWayData(returnStatus, null, null, null);
    }
}
