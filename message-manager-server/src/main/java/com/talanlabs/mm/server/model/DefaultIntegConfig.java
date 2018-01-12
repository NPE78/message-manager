package com.talanlabs.mm.server.model;

import com.talanlabs.mm.shared.model.IIntegConfig;

public class DefaultIntegConfig implements IIntegConfig {

    private String integHost;
    private int integPort;
    private String integApplicationName;

    public DefaultIntegConfig(String integApplicationName) {
        this("localhost", 8080, integApplicationName);
    }

    public DefaultIntegConfig(String integHost, int integPort, String integApplicationName) {
        this.integHost = integHost;
        this.integPort = integPort;
        this.integApplicationName = integApplicationName;
    }

    @Override
    public String getIntegHost() {
        return integHost;
    }

    public void setIntegHost(String integHost) {
        this.integHost = integHost;
    }

    @Override
    public int getIntegPort() {
        return integPort;
    }

    public void setIntegPort(int integPort) {
        this.integPort = integPort;
    }

    @Override
    public String getIntegApplicationName() {
        return integApplicationName;
    }

    public void setIntegApplicationName(String integApplicationName) {
        this.integApplicationName = integApplicationName;
    }
}
