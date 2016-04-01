package com.synaptix.mm.engine.it;

import com.synaptix.mm.shared.model.IIntegConfig;

/**
 * Created by NicolasP on 01/04/2016.
 */
public class DefaultIntegConfig implements IIntegConfig {

	@Override
	public String getIntegFolder() {
		return "flux";
	}

	@Override
	public String getIntegHost() {
		return "localhost";
	}

	@Override
	public int getIntegPort() {
		return 8080;
	}

	@Override
	public String getIntegApplicationName() {
		return "message-manager";
	}
}
