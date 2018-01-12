package com.talanlabs.mm.server.addon;

import com.talanlabs.mm.engine.MMDictionary;
import com.talanlabs.mm.engine.MMEngine;
import com.talanlabs.mm.engine.factory.DefaultProcessErrorFactory;
import com.talanlabs.mm.engine.factory.IProcessErrorFactory;
import com.talanlabs.mm.server.exception.MMEngineException;
import com.talanlabs.mm.shared.model.IMessageType;
import com.talanlabs.processmanager.engine.EngineAddon;
import com.talanlabs.processmanager.engine.ProcessManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MMEngineAddon extends EngineAddon<MMEngineAddon> {

    private final MMEngine mmEngine;
    private final MMDictionary mmDictionary;
    private final Map<String, IMessageType> messageTypeMap;
    private final IProcessErrorFactory processErrorFactory;

    private MMEngineAddon(String engineUuid) {
        this(engineUuid, new DefaultProcessErrorFactory());
    }

    private MMEngineAddon(String engineUuid, IProcessErrorFactory processErrorFactory) {
        super(MMEngineAddon.class, engineUuid);

        mmEngine = new MMEngine();
        mmDictionary = new MMDictionary();
        messageTypeMap = new HashMap<>();

        this.processErrorFactory = processErrorFactory;
    }

    public static MMEngineAddon register(String engineUuid) {
        return new MMEngineAddon(engineUuid).registerAddon();
    }

    public static MMEngineAddon register(String engineUuid, IProcessErrorFactory processErrorFactory) {
        return new MMEngineAddon(engineUuid, processErrorFactory).registerAddon();
    }

    public static MMEngine getEngine(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getEngine).orElseThrow(MMEngineException::new);
    }

    public static MMDictionary getDictionary(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getDictionary).orElseThrow(MMEngineException::new);
    }

    public static IMessageType getMessageType(String engineUuid, String messageType) {
        return getAddon(engineUuid).map(a -> a.getMessageType(messageType)).orElseThrow(MMEngineException::new);
    }

    public static IProcessErrorFactory getProcessErrorFactory(String engineUuid) {
        return getAddon(engineUuid).map(MMEngineAddon::getProcessErrorFactory).orElseThrow(MMEngineException::new);
    }

    private static Optional<MMEngineAddon> getAddon(String engineUuid) {
        return ProcessManager.getEngine(engineUuid).getAddon(MMEngineAddon.class);
    }

    public MMEngine getEngine() {
        return mmEngine;
    }

    public MMDictionary getDictionary() {
        return mmDictionary;
    }

    public IMessageType getMessageType(String messageType) {
        return messageTypeMap.get(messageType);
    }

    public IProcessErrorFactory getProcessErrorFactory() {
        return processErrorFactory;
    }

    @Override
    public void disconnectAddon() {
        // nothing to do here for now
    }
}
