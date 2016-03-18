package com.synaptix.mm.supervision.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.synaptix.mm.supervision.model.AgentInfoDto;
import com.synaptix.pmgr.core.apis.ChannelSlot;
import com.synaptix.pmgr.core.apis.PluggableChannel;
import com.synaptix.pmgr.core.lib.ProcessEngine;
import com.synaptix.pmgr.core.lib.ProcessingChannel;

/**
 * Created by NicolasP on 29/10/2015.
 */
public final class SupervisionUtils {

	private SupervisionUtils() {
	}

	/**
	 * Get the current agent info of all agents used at the present time
	 * @param agentName optional, use this to show only the selected agent info
	 * @return the list of currently used agents with their running count, waiting count... ordered by running+waiting (descending)
	 */
	public static List<AgentInfoDto> getAgentInfo(String agentName) {
		List<AgentInfoDto> agentInfoDtos = new ArrayList<>();
		Collection<ChannelSlot> channels = ProcessEngine.getInstance().getChannels();
		channels.stream().filter(channelSlot -> (agentName == null) || (agentName.isEmpty()) || (agentName.equals(channelSlot.getName()))).forEach(channelSlot -> {
			AgentInfoDto agentInfoDto = new AgentInfoDto();
			agentInfoDto.setName(channelSlot.getName());
			agentInfoDto.setAvailable(channelSlot.isAvailable());
			agentInfoDto.setBusy(channelSlot.isBusy());
			PluggableChannel pluggedChannel = channelSlot.getPluggedChannel();
			if (pluggedChannel instanceof ProcessingChannel) {
				ProcessingChannel processingChannel = (ProcessingChannel) pluggedChannel;
				agentInfoDto.setOverloaded(processingChannel.isOverloaded());
				agentInfoDto.setMeaning(pluggedChannel.toString());
				agentInfoDto.setNbWorking(pluggedChannel.getNbWorking());
				agentInfoDto.setNbWaiting(pluggedChannel.getNbWaiting());
			}
			agentInfoDtos.add(agentInfoDto);
		});
		sortAgentInfo(agentInfoDtos);
		return agentInfoDtos;
	}

	private static void sortAgentInfo(List<AgentInfoDto> agentInfoDtos) {
		Comparator<AgentInfoDto> comparator = (o1, o2) -> {
			return computeBusy(o1).compareTo(computeBusy(o2)) * (-1);
		};
		Collections.sort(agentInfoDtos, comparator);
	}

	private static Integer computeBusy(AgentInfoDto a) {
		return (a.getNbWaiting() != null ? a.getNbWaiting() : 0) + (a.getNbWorking() != null ? a.getNbWorking() : 0);
	}
}
