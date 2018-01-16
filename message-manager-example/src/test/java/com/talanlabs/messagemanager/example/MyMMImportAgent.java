package com.talanlabs.messagemanager.example;

import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.server.AbstractMMImportAgent;
import com.talanlabs.mm.server.model.IErrorEnum;
import com.talanlabs.mm.shared.model.IErrorType;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.ErrorRecyclingKind;
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
    protected void enrichDictionary(SubDictionary subDictionary) {
        registerErrorEnum(MyErrors.class, subDictionary);
    }

    @Override
    protected void acceptMessage() {
    }

    @Override
    protected void rejectMessage() {
    }

    @Override
    protected void prepare(MyImportFlux message) {
        super.prepare(message);

        SubDictionary validationDictionary = getValidationDictionary(message.getDictionary());
        validationDictionary.defineError(IErrorType.of(MyErrors.OLD_ERROR.getCode(), ErrorRecyclingKind.WARNING, null));
    }

    @Override
    protected void saveMessageError(IProcessError processError, ErrorImpact errorImpact) {
        getMessage().addError(processError, errorImpact);
    }

    @Override
    public void process(MyImportFlux message) {
        addError(MyErrors.OLD_ERROR); // should be warning
        switch (message.getContent()) {
            case "AUTOMATIC_ERROR":
                addError(MyErrors.AUTOMATIC_ERROR);
                break;
            case "MANUAL_ERROR":
                addError(MyErrors.MANUAL_ERROR);
                break;
            case "FINAL_ERROR":
                addError(MyErrors.NOT_RECYCLABLE_ERROR);
                break;
            case "WARNING_ERROR":
                addError(MyErrors.WARNING);
                break;
            default:
                break;
        }
    }

    private enum MyErrors implements IErrorEnum {

        WARNING(ErrorRecyclingKind.WARNING),
        NOT_RECYCLABLE_ERROR(ErrorRecyclingKind.NOT_RECYCLABLE),
        AUTOMATIC_ERROR(ErrorRecyclingKind.AUTOMATIC),
        MANUAL_ERROR(ErrorRecyclingKind.MANUAL),
        OLD_ERROR(ErrorRecyclingKind.NOT_RECYCLABLE);

        final ErrorRecyclingKind recyclingKind;

        MyErrors(ErrorRecyclingKind recyclingKind) {
            this.recyclingKind = recyclingKind;
        }

        @Override
        public String getCode() {
            return name();
        }

        @Override
        public ErrorRecyclingKind getRecyclingKind() {
            return recyclingKind;
        }
    }
}
