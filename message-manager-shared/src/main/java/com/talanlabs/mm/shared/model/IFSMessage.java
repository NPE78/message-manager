package com.talanlabs.mm.shared.model;

import java.io.File;

/**
 * Created by NicolasP on 31/03/2016.
 */
public interface IFSMessage extends IMessage {

	/**
	 * The content of the message
	 */
	String getContent();

	/**
	 * The content of the message
	 */
	void setContent(String content);

	/**
	 * The file
	 */
	File getFile();

	/**
	 * The file
	 */
	void setFile(File file);

    /**
     * A sub folder where the file is located.<br>
     * If empty or set, the file finder will also scan archive file of the same name<br>
     * If null, no archive file will be scanned
     */
    String getFolder();

    void setFolder(String var1);

}
