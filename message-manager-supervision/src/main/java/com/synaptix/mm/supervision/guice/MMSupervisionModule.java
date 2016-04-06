package com.synaptix.mm.supervision.guice;

import com.synaptix.mm.supervision.utils.DictionaryUtils;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 06/04/2016.
 */
public class MMSupervisionModule extends AbstractSynaptixIntegratorServletModule {

	@Override
	protected void configure() {
		requestStaticInjection(DictionaryUtils.class);
	}
}
