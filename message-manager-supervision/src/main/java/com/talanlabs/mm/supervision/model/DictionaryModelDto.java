package com.talanlabs.mm.supervision.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by NicolasP on 06/04/2016.
 */
@XmlRootElement
public class DictionaryModelDto {

	private final String dictionaryName;

	private String description;

	private final List<String> errorList;

	private final List<DictionaryModelDto> subDictionaryList;

	public DictionaryModelDto(String dictionaryName) {
		this.dictionaryName = dictionaryName;
		this.errorList = new ArrayList<>();
		this.subDictionaryList = new ArrayList<>();
	}

	public String getDictionaryName() {
		return dictionaryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getErrorList() {
		return new ArrayList<>(errorList);
	}

	public void addError(String error) {
        errorList.add(error);
    }

	public List<DictionaryModelDto> getSubDictionaryList() {
		return new ArrayList<>(subDictionaryList);
	}

	public void addDictionaryModelDto(DictionaryModelDto dictionaryModelDto) {
        subDictionaryList.add(dictionaryModelDto);
    }
}
