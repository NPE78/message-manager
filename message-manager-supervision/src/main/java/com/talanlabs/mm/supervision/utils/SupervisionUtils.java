package com.talanlabs.mm.supervision.utils;

import com.talanlabs.mm.supervision.model.AgentInfoDto;
import com.talanlabs.processmanager.engine.PM;
import com.talanlabs.processmanager.engine.ProcessingChannel;
import com.talanlabs.processmanager.shared.ChannelSlot;
import com.talanlabs.processmanager.shared.Engine;
import com.talanlabs.processmanager.shared.PluggableChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Created by NicolasP on 29/10/2015.
 */
public final class SupervisionUtils {

    private static Comparator<AgentInfoDto> COMPARATOR = (o1, o2) -> o1.getNbWorking().compareTo(o2.getNbWorking()) * (-1);

    private SupervisionUtils() {
    }

    /**
     * Get the current agent info of all agents used at the present time
     *
     * @param agentName optional, use this to show only the selected agent info
     * @return the list of currently used agents with their running count, waiting count... ordered by running+waiting (descending)
     */
    public static List<AgentInfoDto> getAgentInfo(String engineUuid, String agentName) {
        List<AgentInfoDto> agentInfoDtos = new ArrayList<>();
        Engine engine = PM.getEngine(engineUuid);
        Collection<ChannelSlot> channels = engine.getChannelSlots();
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
            }
            agentInfoDtos.add(agentInfoDto);
        });
        sortAgentInfo(agentInfoDtos);
        return agentInfoDtos;
    }

    private static void sortAgentInfo(List<AgentInfoDto> agentInfoDtos) {
        agentInfoDtos.sort(COMPARATOR);
    }
}
