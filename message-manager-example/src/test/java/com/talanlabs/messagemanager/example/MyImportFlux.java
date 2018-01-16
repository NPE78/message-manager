package com.talanlabs.messagemanager.example;

import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import java.util.HashMap;
import java.util.Map;

public class MyImportFlux extends AbstractMMImportFlux {

    private final Map<IProcessError, ErrorImpact> errorMap;

    public MyImportFlux() {
        this.errorMap = new HashMap<>();
    }

    public void addError(IProcessError processError, ErrorImpact errorImpact) {
        errorMap.put(processError, errorImpact);
    }

    public Map<IProcessError, ErrorImpact> getErrors() {
        return new HashMap<>(errorMap);
    }
}
