package com.synaptix.mm.engine.model;

import java.util.List;

import com.synaptix.component.IComponent;
import com.synaptix.mm.shared.model.IProcessError;

/**
 * Created by NicolasP on 23/10/2015.
 */
public interface IProcessContext extends IComponent {

	public List<IProcessError> getProcessErrorList();

	public void setProcessErrorList(List<IProcessError> processErrorList);

}
