package com.bpmnengine;

import com.bpmnengine.data.DataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;
import com.bpmnengine.lang.Lists;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.CorrelationKey;
import com.bpmnengine.model.Definitions;
import com.bpmnengine.model.FlowElements;
import com.bpmnengine.model.FlowNode;
import com.bpmnengine.model.SequenceFlow;
import com.bpmnengine.model.StartEvent;
import com.bpmnengine.model.ThrowEvent;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bpmnengine.token.TokenPredicates.isNotWaiting;

/**
 * @author André Waloszek
 */
public class SubProcessInstance extends ActivityInstance implements ProcessInstance {

    private final List<Token> tokens = new ArrayList<>();

    private final List<ActivityInstance> activityInstances = new LinkedList<>();

    private final Map<String, List<MessageInstance>> messageEventQueues = new HashMap<>();

    private final List<CatchEventTrigger> catchEventTriggers = new LinkedList<>();


    public SubProcessInstance(ProcessInstance processInstance, Activity activity, DataContext dataContext) {
        super(processInstance, activity, dataContext);
        if (!activity.isSubProcess())
            throw new IllegalStateException();
    }

    @Override
    public Definitions getDefinitions() {
        return processInstance.getDefinitions();
    }

    @Override
    public void addToken(Token token) {
        tokens.add(token);
    }

    @Override
    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }

    @Override
    public void addActivityInstance(ActivityInstance activityInstance) {
        activityInstances.add(activityInstance);
    }

    @Override
    public List<ActivityInstance> getActivityInstances() {
        return new ArrayList<>(activityInstances);
    }

    @Override
    public void addCatchEventTrigger(CatchEventTrigger catchEventTrigger) {
        catchEventTriggers.add(catchEventTrigger);
    }

    @Override
    public void removeCatchEventTrigger(CatchEventTrigger catchEventTrigger) {
        catchEventTriggers.remove(catchEventTrigger);
    }

    @Override
    public List<CatchEventTrigger> getCatchEventTriggers(CatchEvent catchEvent) {
        List<CatchEventTrigger> triggers = new LinkedList<>();
        for (CatchEventTrigger trigger : catchEventTriggers) {
            if (Objects.equals(trigger.getCatchEvent(), catchEvent))
                triggers.add(trigger);
        }

        return triggers;

    }

    private List<MessageInstance> getMessageEventQueue(String queueId) {
        List<MessageInstance> messageInstances = messageEventQueues.get(queueId);
        if (messageInstances == null) {
            messageInstances = new LinkedList<>();
            messageEventQueues.put(queueId, messageInstances);
        }
        return messageInstances;
    }


    @Override
    public void queueMessageEvent(String queueId, MessageInstance messageInstance) {
        List<MessageInstance> messageInstanceQueue = getMessageEventQueue(queueId);
        messageInstanceQueue.add(messageInstance);
    }

    @Override
    public MessageInstance dequeMessageEvent(String queueId) {
        List<MessageInstance> messageInstanceQueue = getMessageEventQueue(queueId);
        return messageInstanceQueue.isEmpty() ? null : messageInstanceQueue.remove(0);
    }

    @Override
    public List<SequenceFlow> getOutgoingSequenceFlows(TFlowNode flowNode) {
        return activity.asSubProcess().getSequenceFlows(flowNode.getOutgoing());
    }

    @Override
    public List<SequenceFlow> getIncomingSequenceFlows(TFlowNode flowNode) {
        return activity.asSubProcess().getSequenceFlows(flowNode.getIncoming());
    }

    @Override
    public CompensationHandler getCompensationHandler(Activity activity) {
        return processInstance.getCompensationHandler(activity);
    }

    @Override
    public CorrelationKeyInstance getCorrelationKeyInstance(CorrelationKey correlationKey) {
        return processInstance.getCorrelationKeyInstance(correlationKey);
    }

    @Override
    public void addCorrelationKeyInstance(CorrelationKey correlationKey, CorrelationKeyInstance correlationKeyInstance) {
        processInstance.addCorrelationKeyInstance(correlationKey, correlationKeyInstance);
    }

    @Override
    public void compensate(ThrowEvent throwEvent, Token token) {
        processInstance.compensate(throwEvent, token);
    }

    private List<Token> getWaitingTokens() {
        List<Token> waiting = new LinkedList<>();
        for (Token token : tokens) {
            if (token.getTokenState().equals(TokenState.WAITING))
                waiting.add(token);
        }
        return waiting;
    }

    @Override
    public void run() {
        List<Token> tokenQueue = getWaitingTokens();

        while (!tokenQueue.isEmpty()) {
            Token token = tokenQueue.remove(0);
            FlowNode flowNode = token.getPosition();
            List<Token> moved = flowNode.pass(Collections.singletonList(token), this);
            tokenQueue.addAll(moved);
            tokenQueue.removeAll(Lists.filter(tokenQueue, isNotWaiting()));
        }

        if (!isRunning() && getState().equals(LifecycleState.ACTIVE)) {
            setState(LifecycleState.COMPLETING);
        }
    }

    public boolean isRunning() {
        for (Token token : tokens) {
            if (token.isAlive())
                return true;
        }
        return false;
    }


    @Override
    public TBoundaryEvent getBoundaryEventAttachedToRef(QName actionRef) {
        return FlowElements.getBoundaryEventAttachedToRef(activity.asSubProcess().getFlowElement(), actionRef);
    }

    public List<ActivityInstance> getUserTaskInstances() {
        List<ActivityInstance> userTaskInstances = new LinkedList<>();
        for (ActivityInstance activityInstance : activityInstances) {
            if (activityInstance.getActivity().isUserTask())
                userTaskInstances.add(activityInstance);
        }
        return userTaskInstances;
    }

    public void start() {
        if (!tokens.isEmpty())
            throw new IllegalStateException("Process Instance already running");

        List<StartEvent> startEvents = FlowElements.getStartEventWithoutEventDefinition(activity.asSubProcess().getFlowElement());
        if (startEvents.size() != 1)
            throw new IllegalStateException("Only one StartEvent without EventDefinition is supported");

        StartEvent startEvent = startEvents.get(0);
        Token.create(this, startEvent);
        run();
    }

}
