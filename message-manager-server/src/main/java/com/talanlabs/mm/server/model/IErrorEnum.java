package com.talanlabs.mm.server.model;

import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;

public interface IErrorEnum extends IErrorType {

    String getCode();

    @Override
    default ErrorRecyclingKind getRecyclingKind() {
        return ErrorRecyclingKind.MANUAL;
    }

    @Override
    default Integer getNextRecyclingDuration() {
        return null;
    }
}
