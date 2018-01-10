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
public class ProcessContext implements Serializable {

    private final List<IProcessError> processErrorList;

    private transient String engineUuid;
    private transient IProcessErrorFactory processErrorFactory;

    public ProcessContext() {
        processErrorList = new ArrayList<>(0);
    }

    public String getEngineUuid() {
        return engineUuid;
    }

    public IProcessErrorFactory getProcessErrorFactory() {
        return processErrorFactory;
    }

    public void init(String engineUuid) {
        this.engineUuid = engineUuid;
        this.processErrorFactory = MMEngineAddon.getProcessErrorFactory(engineUuid);
    }

    public void addProcessError(IProcessError processError) {
        processErrorList.add(processError);
    }

    public List<IProcessError> getProcessErrorList() {
        return new ArrayList<>(processErrorList);
    }
}
