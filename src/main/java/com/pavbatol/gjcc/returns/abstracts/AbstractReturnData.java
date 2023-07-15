package com.pavbatol.gjcc.returns.abstracts;

import com.pavbatol.gjcc.returns.ReturnStatus;
import lombok.Data;
import lombok.NonNull;

@Data
public abstract class AbstractReturnData implements StatusHolder {
    @NonNull ReturnStatus status;
}
