package com.sequenceiq.cloudbreak.service.flowlog;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.cedarsoftware.util.io.JsonWriter;
import com.sequenceiq.cloudbreak.cloud.event.Payload;
import com.sequenceiq.cloudbreak.cloud.event.Selectable;
import com.sequenceiq.cloudbreak.core.flow2.FlowState;
import com.sequenceiq.cloudbreak.domain.FlowChainLog;
import com.sequenceiq.cloudbreak.domain.FlowLog;
import com.sequenceiq.cloudbreak.domain.StateStatus;
import com.sequenceiq.cloudbreak.ha.CloudbreakNodeConfig;
import com.sequenceiq.cloudbreak.repository.FlowChainLogRepository;
import com.sequenceiq.cloudbreak.repository.FlowLogRepository;
import com.sequenceiq.cloudbreak.service.TransactionService;
import com.sequenceiq.cloudbreak.service.TransactionService.TransactionExecutionException;

@Service
public class FlowLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogService.class);

    @Inject
    private CloudbreakNodeConfig cloudbreakNodeConfig;

    @Inject
    private FlowLogRepository flowLogRepository;

    @Inject
    private FlowChainLogRepository flowChainLogRepository;

    @Inject
    @Qualifier("JsonWriterOptions")
    private Map<String, Object> writeOptions;

    @Inject
    private TransactionService transactionService;

    public FlowLog save(String flowId, String flowChanId, String key, Payload payload, Map<Object, Object> variables, Class<?> flowType,
            FlowState currentState) {
        String payloadJson = JsonWriter.objectToJson(payload, writeOptions);
        String variablesJson = JsonWriter.objectToJson(variables, writeOptions);
        FlowLog flowLog = new FlowLog(payload.getStackId(), flowId, flowChanId, key, payloadJson, payload.getClass(), variablesJson, flowType,
                currentState.toString());
        flowLog.setCloudbreakNodeId(cloudbreakNodeConfig.getId());
        return flowLogRepository.save(flowLog);
    }

    public FlowLog close(Long stackId, String flowId) throws TransactionExecutionException {
        return finalize(stackId, flowId, "FINISHED");
    }

    public FlowLog cancel(Long stackId, String flowId) throws TransactionExecutionException {
        return finalize(stackId, flowId, "CANCELLED");
    }

    public FlowLog terminate(Long stackId, String flowId) throws TransactionExecutionException {
        return finalize(stackId, flowId, "TERMINATED");
    }

    public void purgeTerminatedStacksFlowLogs() throws TransactionExecutionException {
        transactionService.required(() -> {
            LOGGER.debug("Cleaning deleted stack's flowlog");
            int purgedTerminatedStackLogs = flowLogRepository.purgeTerminatedStackLogs();
            LOGGER.debug("Deleted flowlog count: {}", purgedTerminatedStackLogs);
            LOGGER.debug("Cleaning orphan flowchainlogs");
            int purgedOrphanFLowChainLogs = flowChainLogRepository.purgeOrphanFLowChainLogs();
            LOGGER.debug("Deleted flowchainlog count: {}", purgedOrphanFLowChainLogs);
            return null;
        });
    }

    private FlowLog finalize(Long stackId, String flowId, String state) throws TransactionExecutionException {
        return transactionService.required(() -> {
            flowLogRepository.finalizeByFlowId(flowId);
            updateLastFlowLogStatus(getLastFlowLog(flowId), false);
            FlowLog flowLog = new FlowLog(stackId, flowId, state, Boolean.TRUE, StateStatus.SUCCESSFUL);
            flowLog.setCloudbreakNodeId(cloudbreakNodeConfig.getId());
            return flowLogRepository.save(flowLog);
        });
    }

    public void saveChain(String flowChainId, String parentFlowChainId, Queue<Selectable> chain) {
        String chainJson = JsonWriter.objectToJson(chain);
        FlowChainLog chainLog = new FlowChainLog(flowChainId, parentFlowChainId, chainJson);
        flowChainLogRepository.save(chainLog);
    }

    public void updateLastFlowLogStatus(FlowLog lastFlowLog, boolean failureEvent) {
        StateStatus stateStatus = failureEvent ? StateStatus.FAILED : StateStatus.SUCCESSFUL;
        flowLogRepository.updateLastLogStatusInFlow(lastFlowLog.getId(), stateStatus);
    }

    public boolean isOtherFlowRunning(Long stackId) {
        Set<String> flowIds = flowLogRepository.findAllRunningNonTerminationFlowIdsByStackId(stackId);
        return !flowIds.isEmpty();
    }

    public boolean repeatedFlowState(FlowLog lastFlowLog, String event) {
        return lastFlowLog.getNextEvent().equalsIgnoreCase(event);
    }

    public void updateLastFlowLogPayload(FlowLog lastFlowLog, Payload payload, Map<Object, Object> variables) {
        String payloadJson = JsonWriter.objectToJson(payload, writeOptions);
        String variablesJson = JsonWriter.objectToJson(variables, writeOptions);
        Optional.ofNullable(lastFlowLog)
                .ifPresent(flowLog -> {
                    flowLog.setPayload(payloadJson);
                    flowLog.setVariables(variablesJson);
                    flowLogRepository.save(flowLog);
                });
    }

    public FlowLog getLastFlowLog(String flowId) {
        return flowLogRepository.findFirstByFlowIdOrderByCreatedDesc(flowId);
    }
}
