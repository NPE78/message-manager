package com.talanlabs.messagemanager.example;

import com.talanlabs.mm.server.AbstractMMImportAgent;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.SerializationUtils;

public class MyMMImportAgent extends AbstractMMImportAgent<MyImportFlux> {

    private final Map<Serializable, MyImportFlux> messages;

    public MyMMImportAgent(Map<Serializable, MyImportFlux> messages) {
        super(MyImportFlux.class);

        this.messages = messages;
    }

    @Override
    protected MyImportFlux createFlux() {
        return new MyImportFlux();
    }

    @Override
    protected void saveOrUpdateMessage(MyImportFlux message) {
        if (message.getId() == null) {
            // simulation of first save
            message.setId(UUID.randomUUID());
            // end of simulation

        }
        messages.put(message.getId(), SerializationUtils.clone(message)); //commit changes to list
    }

    @Override
    protected void acceptMessage() {

    }

    @Override
    protected void rejectMessage() {

    }

    @Override
    protected void saveMessageError(IProcessError processError, ErrorImpact errorImpact) {

    }

    @Override
    public void process(MyImportFlux message) {
        switch (message.getContent()) {
            case "AUTOMATIC_ERROR":
                addError("automaticError");
                break;
            case "MANUAL_ERROR":
                addError("manualError");
                break;
            case "FINAL_ERROR":
                addError("notRecyclableError");
                break;
            case "WARNING_ERROR":
                addError("warningError");
                break;
            default:
                break;
        }
    }
}
