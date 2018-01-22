package com.bpmnengine.model;

import com.bpmnengine.CatchEventTrigger;
import com.bpmnengine.CorrelationKeyInstance;
import com.bpmnengine.MessageInstance;
import com.bpmnengine.ProcessInstance;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;
import com.bpmnengine.data.CatchEventDataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TCatchEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TCompensateEventDefinition;
import com.bpmnengine.gen.omg.spec.bpmn.model.TEventDefinition;
import com.bpmnengine.gen.omg.spec.bpmn.model.TMessage;
import com.bpmnengine.gen.omg.spec.bpmn.model.TMessageEventDefinition;
import com.bpmnengine.lang.ListBuilder;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.bpmnengine.lang.Predicates.isType;

/**
 * @author André Waloszek
 */
public class CatchEvent extends TCatchEvent {

    public boolean hasEventDefinition() {
        return !getEventDefinition().isEmpty();
    }

    public boolean hasMessageEventDefinition() {
        return getMessageEventDefinition() != null;
    }

    public TMessageEventDefinition getMessageEventDefinition() {
        return (TMessageEventDefinition) ListBuilder.of(getEventDefinition())
                .transform(Functions.jaxbElementToTEventDefinition())
                .filter(isType(TMessageEventDefinition.class))
                .first()
                .toElement();
    }

    public boolean hasCompensateEventDefinition() {
        return getCompensateEventDefinition() != null;
    }

    public TCompensateEventDefinition getCompensateEventDefinition() {
        return (TCompensateEventDefinition) ListBuilder.of(getEventDefinition())
                .transform(Functions.jaxbElementToTEventDefinition())
                .filter(isType(TCompensateEventDefinition.class))
                .first()
                .toElement();
    }


    @Override
    public boolean isCatchEvent() {
        return true;
    }

    @Override
    public CatchEvent asCatchEvent() {
        return this;
    }


    public boolean catches(MessageInstance messageInstance) {
        List<JAXBElement<? extends TEventDefinition>> eventDefinitions = getEventDefinition();

        for (JAXBElement<? extends TEventDefinition> eventDefinition : eventDefinitions) {
            TEventDefinition eventDefinitionValue = eventDefinition.getValue();

            if (eventDefinitionValue instanceof TMessageEventDefinition) {
                TMessageEventDefinition messageEventDefinition = (TMessageEventDefinition) eventDefinitionValue;

                if (messageInstance.getMessageId().equals(messageEventDefinition.getMessageRef().getLocalPart()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> waiting = getTokensForNodeWaiting(tokens, processInstance);

        if (waiting.size() > 1)
            throw new IllegalStateException("There should be only one token waiting for an IntermediateCatchEvent");

        if (!hasEventDefinition()) {
            if (waiting.size() != 1)
                throw new IllegalStateException("There must be exactly one token waiting for an Events without EventDefinition");
            return generateTokens(processInstance, waiting.get(0));
        }

        if (waiting.size() == 1) {
            Token token = waiting.get(0);
            token.setTokenState(TokenState.WAITING_FOR_TRIGGER);
            processInstance.addCatchEventTrigger(new CatchEventTrigger(this, token));
        }

        List<CatchEventTrigger> triggers = processInstance.getCatchEventTriggers(this);
        List<Token> allOutgoingTokens = new LinkedList<>();

        for (CatchEventTrigger trigger : triggers) {
            MessageInstance messageInstance = processInstance.dequeMessageEvent(id);
            if (messageInstance == null)
                break;

            CatchEventDataContext dataContext = processInstance.getDataContext().createDataContext(this);
            dataContext.execute(messageInstance.getMessageData());

            Token token = trigger.getToken();

            allOutgoingTokens.addAll(generateTokens(processInstance, token));
            processInstance.removeCatchEventTrigger(trigger);
        }

        return allOutgoingTokens;
    }

    @Override
    protected List<Token> getTokensForNodeWaiting(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> tokensForNodeWaiting = super.getTokensForNodeWaiting(tokens, processInstance);

        if (!hasMessageEventDefinition())
            return tokensForNodeWaiting;

        for (Token token : getTokensForNodeWaitingForTrigger(tokens, processInstance)) {
            if (!tokensForNodeWaiting.contains(token))
                tokensForNodeWaiting.add(token);

        }
        return tokensForNodeWaiting;
    }


    private List<Token> getTokensForNodeWaitingForTrigger(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> waiting = new LinkedList<>();
        for (Token token : processInstance.getTokens()) {
            if (token.getPosition().equals(this) && token.getTokenState().equals(TokenState.WAITING_FOR_TRIGGER) && !waiting.contains(token)) {
                waiting.add(token);
            }
        }
        return waiting;
    }


    public List<Token> generateTokens(ProcessInstance processInstance, Token incomingToken) {
        List<SequenceFlow> outgoing = processInstance.getOutgoingSequenceFlows(this);
        List<Token> outgoingTokens;

        if (outgoing.size() == 1) {
            outgoingTokens = Collections.singletonList(incomingToken);
            incomingToken.setTokenState(TokenState.WAITING);
        } else
            outgoingTokens = Token.diverge(processInstance, incomingToken, outgoing.size());

        for (int i = 0; i < outgoing.size(); i++)
            outgoingTokens.get(i).move(processInstance.getOutgoingSequenceFlows(this).get(i));

        return outgoingTokens;
    }


    public boolean correlates(ProcessInstance processInstance, MessageInstance messageInstance) {
        if (!hasMessageEventDefinition())
            throw new IllegalStateException("CatchEvent has no MessageEventDefinition");

        TMessageEventDefinition messageEventDefinition = getMessageEventDefinition();
        QName messageRef = messageEventDefinition.getMessageRef();

        // check if there is a key for the the message id
        TMessage message = processInstance.getDefinitions().getMessage(messageRef);

        List<CorrelationKey> correlationKeys = processInstance.getDefinitions().getCollaboration().getCorrelationKey();
        if (correlationKeys.size() == 0) {
            processInstance.queueMessageEvent(id, messageInstance);
            return true;
        }

        if (correlationKeys.size() > 1)
            throw new UnsupportedOperationException();

        CorrelationKey correlationKey = correlationKeys.get(0);

        CorrelationKeyInstance correlationKeyInstance = processInstance.getDefinitions().createCorrelationKeyInstance(correlationKey);
        for (CorrelationProperty correlationProperty : processInstance.getDefinitions().getCorrelationProperties(correlationKey)) {
            Object value = correlationProperty.getCorrelationPropertyRetrievelExpression(message.getId()).getMessagePath().eval(messageInstance.getMessageData());
            correlationKeyInstance.setProperty(correlationProperty.getId(), value);
        }

        CorrelationKeyInstance existingCorrelationKeyInstance = processInstance.getCorrelationKeyInstance(correlationKey);

        if (existingCorrelationKeyInstance != null) {
            boolean correlates = Objects.equals(correlationKeyInstance, existingCorrelationKeyInstance);
            if (correlates)
                processInstance.queueMessageEvent(id, messageInstance);
            return correlates;
        }

        processInstance.addCorrelationKeyInstance(correlationKey, correlationKeyInstance);
        processInstance.queueMessageEvent(id, messageInstance);
        return true;
    }

}
