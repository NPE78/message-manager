package com.talanlabs.mm.server.agent;

import com.talanlabs.mm.server.AbstractMMImportAgent;
import com.talanlabs.mm.server.model.AbstractMMImportFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import java.time.Instant;

public class DefaultMMImportAgentTest<F extends AbstractMMImportFlux> extends AbstractMMImportAgent<F> {

    /**
     * Create an agent with given name
     *
     * @param fluxClass the managed flux class from which the agent name is deducted
     */
    public DefaultMMImportAgentTest(Class<F> fluxClass) {
        super(fluxClass);
    }

    @Override
    protected F createFlux() {
        return null;
    }

    @Override
    protected void saveOrUpdateMessage(F message) {
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
    public void process(F message) {
    }

    @Override
    public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
    }
}
