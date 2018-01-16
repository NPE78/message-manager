package com.talanlabs.mm.server.implem;

import com.talanlabs.mm.server.AbstractMMAgent;
import com.talanlabs.mm.server.model.AbstractMMFlux;
import com.talanlabs.mm.shared.model.IProcessError;
import com.talanlabs.mm.shared.model.domain.ErrorImpact;
import com.talanlabs.mm.shared.model.domain.MessageWay;
import java.util.Map;

/**
 * Created by NicolasP on 26/10/2015.
 */
public class DefaultMMAgent<F extends AbstractMMFlux> extends AbstractMMAgent<F> {

    public DefaultMMAgent(Class<F> fluxClass) {
        super(fluxClass);
    }

    @Override
    protected void saveOrUpdateMessage(F message) {
        // do stuff
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
