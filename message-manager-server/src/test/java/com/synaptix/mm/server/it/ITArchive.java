package com.synaptix.mm.server.it;

import org.junit.Test;

import com.synaptix.mm.server.agent.BatchArchiveAgent;

/**
 * Created by NicolasP on 31/03/2016.
 */
public class ITArchive extends AbstractMMTest {

	@Test
	public void testArchive() {
		getInstance(BatchArchiveAgent.class).work(null, null);
	}
}
