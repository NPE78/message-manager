package com.synaptix.mm.server.model;

import java.util.List;

import com.synaptix.component.IComponent;
import com.synaptix.mm.shared.model.IProcessError;

/**
 * This component is the lowest level of what needs to be a process context: a list of errors raised during the process
 * Created by NicolasP on 23/10/2015.
 */
public interface IProcessContext extends IComponent {

	List<IProcessError> getProcessErrorList();

	void setProcessErrorList(List<IProcessError> processErrorList);

}
