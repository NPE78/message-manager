package com.synaptix.mm.shared.model;

import com.synaptix.mm.shared.model.domain.MessageWay;

/**
 * Created by NicolasP on 22/10/2015.
 */
public interface IMessageType {

	public String getName();

	public MessageWay getMessageWay();

	public Integer getRecyclingDeadline();

}
