package com.bpmnengine;

import com.bpmnengine.data.ProcessDataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TCompensateEventDefinition;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;
import com.bpmnengine.lang.Lists;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.BPMNModel;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.CorrelationKey;
import com.bpmnengine.model.Definitions;
import com.bpmnengine.model.FlowElements;
import com.bpmnengine.model.FlowNode;
import com.bpmnengine.model.Process;
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
public class GlobalProcessInstance implements ProcessInstance {

    private final BPMNModel model;

    private final Definitions definitions;

    private final Process process;

    private final List<Token> tokens = new ArrayList<>();

    private final String id;

    private final ProcessDataContext processDataContext;

    private final List<ActivityInstance> activityInstances = new LinkedList<>();

    private final Map<String, CorrelationKeyInstance> correlationKeyInstanceById = new HashMap<>();

    private final Map<String, List<MessageInstance>> messageQueues = new HashMap<>();

    private final List<CatchEventTrigger> catchEventTriggers = new LinkedList<>();

    private final Map<String, CompensationHandler> compensationHandlerByActivityId = new HashMap<>();

    private LifecycleState state = LifecycleState.INACTIVE;

    public GlobalProcessInstance(String id, BPMNModel model) {
        this.id = id;
        this.model = model;
        this.definitions = model.getDefinitions();
        this.process = model.getDefinitions().getExecutableProcess();
        this.processDataContext = new ProcessDataContext(process);
    }

    public String getId() {
        return id;
    }

    public BPMNModel getModel() {
        return model;
    }

    public boolean isRunning() {
        for (Token token : tokens) {
            if (token.isAlive())
                return true;
        }
        return false;
    }

    public boolean hasTokenAt(TFlowNode node) {
        for (Token token : tokens)
            if (token.isAlive())
                if (Objects.equals(token.getPosition(), node))
                    return true;
        return false;
    }

    public boolean trigger(MessageInstance messageInstance) {
        boolean triggered = false;

        List<CatchEvent> catchEvents = process.catches(messageInstance);
        for (CatchEvent catchEvent : catchEvents) {
            if (catchEvent.correlates(this, messageInstance))
                triggered = true;
        }

        run();

        return triggered;
    }

    private List<Token> getWaitingTokens() {
        List<Token> waiting = new LinkedList<>();
        for (Token token : tokens) {
            if (token.getTokenState().equals(TokenState.WAITING) || token.getTokenState().equals(TokenState.WAITING_FOR_TRIGGER))
                waiting.add(token);
        }
        return waiting;
    }

    public synchronized void run() {
        List<Token> tokenQueue = getWaitingTokens();

        while (!tokenQueue.isEmpty()) {
            Token token = tokenQueue.remove(0);
            FlowNode flowNode = token.getPosition();
            List<Token> moved = flowNode.pass(Collections.singletonList(token), this);
            tokenQueue.addAll(moved);
            tokenQueue.removeAll(Lists.filter(tokenQueue, isNotWaiting()));
        }

        if (!isRunning() && getState().equals(LifecycleState.ACTIVE)) {
            setState(LifecycleState.COMPLETED);
        }
    }

    @Override
    public TBoundaryEvent getBoundaryEventAttachedToRef(QName actionRef) {
        return FlowElements.getBoundaryEventAttachedToRef(process.getFlowElement(), actionRef);
    }

    public List<ActivityInstance> getActivityInstances() {
        return new ArrayList<>(activityInstances);
    }

    public void addActivityInstance(ActivityInstance activityInstance) {
        activityInstances.add(activityInstance);
    }

    public ProcessDataContext getDataContext() {
        return processDataContext;
    }

    public CorrelationKeyInstance getCorrelationKeyInstance(CorrelationKey correlationKey) {
        return correlationKeyInstanceById.get(correlationKey.getId());
    }

    public void addCorrelationKeyInstance(CorrelationKey correlationKey, CorrelationKeyInstance correlationKeyInstance) {
        if (correlationKeyInstanceById.containsKey(correlationKey.getId()))
            throw new IllegalStateException("CorrelationKeyInstance already exists for CorrelationKey " + correlationKey.getId());
        correlationKeyInstanceById.put(correlationKey.getId(), correlationKeyInstance);
    }

    private List<MessageInstance> getMessageEventQueue(String queueId) {
        List<MessageInstance> messageInstances = messageQueues.get(queueId);
        if (messageInstances == null) {
            messageInstances = new LinkedList<>();
            messageQueues.put(queueId, messageInstances);
        }
        return messageInstances;
    }

    public void queueMessageEvent(String queueId, MessageInstance messageInstance) {
        List<MessageInstance> messageInstanceQueue = getMessageEventQueue(queueId);
        messageInstanceQueue.add(messageInstance);
    }

    public MessageInstance dequeMessageEvent(String queueId) {
        List<MessageInstance> messageInstanceQueue = getMessageEventQueue(queueId);
        return messageInstanceQueue.isEmpty() ? null : messageInstanceQueue.remove(0);

    }

    @Override
    public List<SequenceFlow> getOutgoingSequenceFlows(TFlowNode flowNode) {
        return process.getSequenceFlows(flowNode.getOutgoing());
    }

    @Override
    public List<SequenceFlow> getIncomingSequenceFlows(TFlowNode flowNode) {
        return process.getSequenceFlows(flowNode.getIncoming());
    }

    public void addCatchEventTrigger(CatchEventTrigger catchEventTrigger) {
        catchEventTriggers.add(catchEventTrigger);
    }

    public List<CatchEventTrigger> getCatchEventTriggers(CatchEvent catchEvent) {
        List<CatchEventTrigger> triggers = new LinkedList<>();
        for (CatchEventTrigger trigger : catchEventTriggers) {
            if (Objects.equals(trigger.getCatchEvent(), catchEvent))
                triggers.add(trigger);
        }

        return triggers;
    }

    public void removeCatchEventTrigger(CatchEventTrigger catchEventTrigger) {
        catchEventTriggers.remove(catchEventTrigger);
    }

    @Override
    public Definitions getDefinitions() {
        return definitions;
    }

    public synchronized void addToken(Token token) {
        if (LifecycleState.INACTIVE.equals(state))
            setState(LifecycleState.ACTIVE);
        tokens.add(token);
    }

    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }

    public LifecycleState getState() {
        return state;
    }

    public void setState(LifecycleState processState) {
        this.state = processState;
    }

    public List<UserTaskInstance> getActiveUserTaskInstances() {
        List<UserTaskInstance> userTaskInstances = new LinkedList<>();
        for (ActivityInstance activityInstance : activityInstances) {
            if (activityInstance.getActivity().isUserTask() && activityInstance.getState().equals(LifecycleState.ACTIVE))
                userTaskInstances.add(new UserTaskInstance(activityInstance));
        }
        return userTaskInstances;
    }

    public CompensationHandler getCompensationHandler(Activity activity) {
        CompensationHandler compensationHandler = compensationHandlerByActivityId.get(activity.getId());
        if (compensationHandler == null) {
            compensationHandler = CompensationHandler.create(this, activity);
            if (compensationHandler != null)
                compensationHandlerByActivityId.put(activity.getId(), compensationHandler);
        }

        return compensationHandler;
    }

    public void compensate(ThrowEvent throwEvent, Token token) {
        if (!throwEvent.hasCompensateEventDefinition())
            throw new IllegalArgumentException("ThrowEvent must have a CompensateEventDefinition");

        TCompensateEventDefinition compensateEventDefinition = throwEvent.getCompensateEventDefinition();
        QName activityRef = compensateEventDefinition.getActivityRef();
        CompensationHandler compensationHandler = compensationHandlerByActivityId.get(activityRef.toString());
        compensationHandler.compensate(throwEvent, token);
    }

    public void start() {
        if (!tokens.isEmpty())
            throw new IllegalStateException("Process Instance already running");

        List<StartEvent> startEvents = process.getStartEventWithoutEventDefinition();
        if (startEvents.size() != 1)
            throw new IllegalStateException("Only one StartEvent without EventDefinition is supported");

        StartEvent startEvent = startEvents.get(0);
        Token.create(this, startEvent);
        run();
    }
}