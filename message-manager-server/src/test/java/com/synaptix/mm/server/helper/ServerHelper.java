package com.synaptix.mm.server.helper;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;

/**
 * Created by NicolasP on 31/03/2016.
 */
public class ServerHelper {

	public static void configureServer() {
		Locale.setDefault(Locale.FRANCE);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		DateTimeZone.setDefault(DateTimeZone.forID("UTC"));
	}
}
