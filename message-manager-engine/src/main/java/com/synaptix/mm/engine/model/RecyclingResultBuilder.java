package com.synaptix.mm.engine.model;

import java.util.Date;

import com.synaptix.component.factory.ComponentFactory;
import com.synaptix.mm.shared.model.domain.ErrorRecyclingKind;

/**
 * Created by NicolasP on 22/10/2015.
 */
public final class RecyclingResultBuilder {

	private RecyclingResultBuilder() {
	}

	public static IRecyclingResult accept() {
		IRecyclingResult recyclingResult = ComponentFactory.getInstance().createInstance(IRecyclingResult.class);
		recyclingResult.setState(IRecyclingResult.State.VALID);
		return recyclingResult;
	}

	public static IRecyclingResult reject(ErrorRecyclingKind errorRecyclingKind, Date nextProcessingDate) {
		IRecyclingResult recyclingResult = ComponentFactory.getInstance().createInstance(IRecyclingResult.class);
		recyclingResult.setState(IRecyclingResult.State.INVALID);
		recyclingResult.setErrorRecyclingKind(errorRecyclingKind);
		recyclingResult.setNextProcessingDate(nextProcessingDate);
		return recyclingResult;
	}
}
