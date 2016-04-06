package com.synaptix.mm.supervision.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by NicolasP on 06/04/2016.
 */
@XmlRootElement
public class DictionaryModelDto {

	final String dictionaryName;

	final List<String> errorList;

	final List<DictionaryModelDto> subDictionaryList;

	public DictionaryModelDto(String dictionaryName) {
		this.dictionaryName = dictionaryName;
		this.errorList = new ArrayList<>();
		this.subDictionaryList = new ArrayList<>();
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public List<String> getErrorList() {
		return errorList;
	}

	public List<DictionaryModelDto> getSubDictionaryList() {
		return subDictionaryList;
	}
}
