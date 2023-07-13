package com.pavbatol.gjcc.returns;

import lombok.NonNull;
import lombok.Value;

@Value
public class ReturnLoadingFildsWayData {
    @NonNull ReturnStatus status;
    Boolean allFields;
    Boolean specifiedFields;
    String[] inputFields;

    public static ReturnLoadingFildsWayData of(ReturnStatus returnStatus) {
        return new ReturnLoadingFildsWayData(returnStatus, null, null, null);
    }
}
