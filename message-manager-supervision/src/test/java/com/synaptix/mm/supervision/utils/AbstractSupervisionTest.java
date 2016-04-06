package com.synaptix.mm.supervision.utils;

import com.synaptix.mm.server.it.AbstractMMTest;
import com.synaptix.mm.supervision.guice.MMSupervisionModule;
import com.synaptix.pmgr.guice.AbstractSynaptixIntegratorServletModule;

/**
 * Created by NicolasP on 06/04/2016.
 */
public class AbstractSupervisionTest extends AbstractMMTest {

	@Override
	protected AbstractSynaptixIntegratorServletModule buildIntegratorTestModule() {
		return new MMSupervisionModule();
	}
}
