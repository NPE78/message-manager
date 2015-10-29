package com.synaptix.mm.supervision.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for an agent
 * Created by NicolasP on 22/10/2015.
 */
@XmlRootElement
public final class AgentInfoDto {

	private String name;
	private Boolean available;
	private Boolean busy;
	private Boolean overloaded;
	private String meaning;
	private Integer nbWorking;
	private Integer nbWaiting;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	public Boolean isBusy() {
		return busy;
	}

	public void setBusy(Boolean busy) {
		this.busy = busy;
	}

	public Boolean isOverloaded() {
		return overloaded;
	}

	public void setOverloaded(Boolean overloaded) {
		this.overloaded = overloaded;
	}

	public String getMeaning() {
		return meaning;
	}

	public void setMeaning(String meaning) {
		this.meaning = meaning;
	}

	public Integer getNbWorking() {
		return nbWorking;
	}

	public void setNbWorking(Integer nbWorking) {
		this.nbWorking = nbWorking;
	}

	public Integer getNbWaiting() {
		return nbWaiting;
	}

	public void setNbWaiting(Integer nbWaiting) {
		this.nbWaiting = nbWaiting;
	}
}
