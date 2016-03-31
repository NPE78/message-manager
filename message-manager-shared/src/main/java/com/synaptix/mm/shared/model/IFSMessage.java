package com.synaptix.mm.shared.model;

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
	 * The folder
	 */
	String getFolder();

	/**
	 * The folder
	 */
	void setFolder(String folder);

}
