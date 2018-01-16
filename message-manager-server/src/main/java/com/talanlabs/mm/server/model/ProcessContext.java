package com.talanlabs.mm.server.model;

import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.addon.MMEngineAddon;
import com.talanlabs.mm.shared.model.IProcessError;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This POJO is the lowest level of what needs to be a process context: a list of errors raised during the process
 */
public final class ProcessContext implements Serializable {

    private final List<IProcessError> processErrorList;

    private transient IProcessErrorFactory processErrorFactory;

    ProcessContext() {
        processErrorList = new ArrayList<>(0);
    }

    public IProcessErrorFactory getProcessErrorFactory() {
        return processErrorFactory;
    }

    public void init(String engineUuid) {
        this.processErrorFactory = MMEngineAddon.getProcessErrorFactory(engineUuid);
    }

    public void addProcessError(IProcessError processError) {
        processErrorList.add(processError);
    }

    public List<IProcessError> getProcessErrorList() {
        return new ArrayList<>(processErrorList);
    }
}
