package com.talanlabs.mm.server.implem;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.SubDictionary;
import com.talanlabs.mm.server.AbstractMMAgent;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageStatus;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import java.time.Instant;
import java.util.Map;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMMAgent<F extends AbstractMMFlux> extends AbstractMMAgent<F> {

    public DefaultMMAgent(Class<F> fluxClass) {
        super(fluxClass);
    }

    @Override
    public void notifyMessageStatus(MessageStatus newMessageStatus, Instant nextProcessingDate) {
        getMessage().setMessageStatus(newMessageStatus);
        getMessage().setNextProcessingDate(nextProcessingDate);
    }

    @Override
    public void process(F message) {
        // do stuff
    }

    @Override
    public void reject(Map<IProcessError, ErrorImpact> errorMap) {
        // do stuff
    }

    @Override
    public void accept(Map<IProcessError, ErrorImpact> errorMap) {
        // do stuff
    }

    @Override
    public SubDictionary getValidationDictionary(MMDictionary rootDictionary) {
        return rootDictionary;
    }

    @Override
    public MessageWay getMessageWay() {
        return MessageWay.IN;
    }

    public static class DefaultSimpleMMAgent extends DefaultMMAgent<DefaultFlux> {

        public DefaultSimpleMMAgent() {
            super(DefaultFlux.class);
        }
    }

    public static class DefaultFlux extends AbstractMMFlux {

    }
}
