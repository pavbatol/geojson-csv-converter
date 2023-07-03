package com.pavbatol.gjcc.converter;

import lombok.Value;

@Value
public class ReturnLoadingFildsWayData {
    ReturnStatus status;
    Boolean allFields;
    Boolean specifiedFields;
    String[] inputFields;

    public static ReturnLoadingFildsWayData of(ReturnStatus returnStatus) {
        return new ReturnLoadingFildsWayData(returnStatus, null, null, null);
    }
}
