package com.bpmnengine;

import com.bpmnengine.data.DataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.CorrelationKey;
import com.bpmnengine.model.Definitions;
import com.bpmnengine.model.SequenceFlow;
import com.bpmnengine.model.ThrowEvent;
import com.bpmnengine.token.Token;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author André Waloszek
 */
public interface ProcessInstance {

    Definitions getDefinitions();

    void addToken(Token token);

    List<Token> getTokens();

    DataContext getDataContext();

    void addActivityInstance(ActivityInstance activityInstance);

    List<ActivityInstance> getActivityInstances();

    void addCatchEventTrigger(CatchEventTrigger catchEventTrigger);

    void removeCatchEventTrigger(CatchEventTrigger catchEventTrigger);

    List<CatchEventTrigger> getCatchEventTriggers(CatchEvent catchEvent);

    void queueMessageEvent(String queueId, MessageInstance messageInstance);

    MessageInstance dequeMessageEvent(String queueId);

    List<SequenceFlow> getOutgoingSequenceFlows(TFlowNode flowNode);

    List<SequenceFlow> getIncomingSequenceFlows(TFlowNode flowNode);

    CompensationHandler getCompensationHandler(Activity activity);

    void setState(LifecycleState processState);

    LifecycleState getState();

    CorrelationKeyInstance getCorrelationKeyInstance(CorrelationKey correlationKey);

    void addCorrelationKeyInstance(CorrelationKey correlationKey, CorrelationKeyInstance correlationKeyInstance);

    void compensate(ThrowEvent throwEvent, Token token);

    void run();

    TBoundaryEvent getBoundaryEventAttachedToRef(QName actionRef);
}
