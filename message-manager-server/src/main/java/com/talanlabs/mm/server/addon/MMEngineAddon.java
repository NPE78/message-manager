package com.talanlabs.mm.server.addon;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.MMEngine;
import com.talanlabs.mm.engine.factory.DefaultProcessErrorFactory;
import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.IServer;
import com.talanlabs.mm.server.delegate.FluxContentManager;
import com.talanlabs.mm.server.exception.MMEngineException;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.processmanager.engine.EngineAddon;
import com.talanlabs.processmanager.engine.PM;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MMEngineAddon extends EngineAddon<MMEngineAddon> {

    private final IServer server;
    private final MMEngine mmEngine;
    private final MMDictionary mmDictionary;
    private final Map<String, IMessageType> messageTypeMap;
    private final FluxContentManager fluxContentManager;
    private IProcessErrorFactory processErrorFactory;

    private MMEngineAddon(String engineUuid, IServer server) {
        super(MMEngineAddon.class, engineUuid);

        this.server = server;

        mmEngine = new MMEngine();
        mmDictionary = new MMDictionary();
        messageTypeMap = new HashMap<>();
        fluxContentManager = new FluxContentManager();

        this.processErrorFactory = new DefaultProcessErrorFactory();
    }

    public static MMEngineAddon register(String engineUuid, IServer server) {
        return new MMEngineAddon(engineUuid, server).registerAddon();
    }

    public static MMEngine getEngine(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getEngine).orElseThrow(MMEngineException::new);
    }

    public static MMDictionary getDictionary(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getDictionary).orElseThrow(MMEngineException::new);
    }

    public static FluxContentManager getFluxContentManager(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getFluxContentManager).orElseThrow(MMEngineException::new);
    }

    public static IMessageType getMessageType(String engineUuid, String messageType) {
        return getAddon(engineUuid).map(a -> a.getMessageType(messageType)).orElseThrow(MMEngineException::new);
    }

    public static IProcessErrorFactory getProcessErrorFactory(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getProcessErrorFactory).orElseThrow(MMEngineException::new);
    }

    public static void setProcessErrorFactory(String engineUuid, IProcessErrorFactory processErrorFactory) {
        MMEngineAddon addon = getAddon(engineUuid).orElseThrow(MMEngineException::new);
        addon.setProcessErrorFactory(processErrorFactory);
    }

    private static Optional<MMEngineAddon> getAddon(String engineUuid) {
        return PM.getEngine(engineUuid).getAddon(MMEngineAddon.class);
    }

    public MMEngine getEngine() {
        return mmEngine;
    }

    public MMDictionary getDictionary() {
        return mmDictionary;
    }

    public FluxContentManager getFluxContentManager() {
        return fluxContentManager;
    }

    public IMessageType getMessageType(String messageType) {
        return messageTypeMap.get(messageType);
    }

    public static void registerMessageType(String engineUuid, IMessageType messageType) {
        MMEngineAddon addon = getAddon(engineUuid).orElseThrow(MMEngineException::new);
        addon.registerMessageType(messageType);
    }

    private void registerMessageType(IMessageType messageType) {
        messageTypeMap.put(messageType.getName(), messageType);
    }

    private IProcessErrorFactory getProcessErrorFactory() {
        return processErrorFactory;
    }

    public void setProcessErrorFactory(IProcessErrorFactory processErrorFactory) {
        this.processErrorFactory = processErrorFactory;
    }

    @Override
    public void disconnectAddon() {
        server.stop();
    }
}
