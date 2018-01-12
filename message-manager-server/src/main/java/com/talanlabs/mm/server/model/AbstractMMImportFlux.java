package com.talanlabs.mm.server.model;

import com.talanlabs.mm.shared.model.IFSMessage;
import com.talanlabs.processmanager.messages.flux.IImportFlux;
import java.io.File;

public abstract class AbstractMMImportFlux extends AbstractMMFlux implements IImportFlux, IFSMessage {

    private String content;
    private File file;
    private String folder;

    @Override
    public final String getContent() {
        return content;
    }

    @Override
    public final void setContent(String content) {
        this.content = content;
    }

    @Override
    public final File getFile() {
        return file;
    }

    @Override
    public final void setFile(File file) {
        this.file = file;
    }

    @Override
    public final String getFolder() {
        return folder;
    }

    @Override
    public final void setFolder(String folder) {
        this.folder = folder;
    }
}
