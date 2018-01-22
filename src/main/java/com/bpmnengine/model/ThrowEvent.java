package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;
import com.bpmnengine.gen.omg.spec.bpmn.model.TCompensateEventDefinition;
import com.bpmnengine.gen.omg.spec.bpmn.model.TThrowEvent;
import com.bpmnengine.lang.ListBuilder;

import java.util.Collections;
import java.util.List;

import static com.bpmnengine.lang.Predicates.isType;

/**
 * @author André Waloszek
 */
public class ThrowEvent extends TThrowEvent {

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
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        tokens = getTokensForNodeWaiting(tokens, processInstance);

        if (tokens.size() != 1)
            throw new IllegalStateException();

        if (hasCompensateEventDefinition()) {
            processInstance.compensate(this, tokens.get(0));

            TCompensateEventDefinition compensateEventDefinition = getCompensateEventDefinition();
            return compensateEventDefinition.isWaitForCompletion()
                    ? Collections.<Token>emptyList() : generateTokens(processInstance, tokens.get(0));

        } else
            throw new UnsupportedOperationException("EventDefinition not supported");
    }

    public List<Token> generateTokens(ProcessInstance processInstance, Token incomingToken) {
        List<SequenceFlow> outgoing = processInstance.getOutgoingSequenceFlows(this);
        List<Token> outgoingTokens;

        if (outgoing.size() == 0) {
            incomingToken.setTokenState(TokenState.END);
            return Collections.emptyList();

        } else if (outgoing.size() == 1) {
            outgoingTokens = Collections.singletonList(incomingToken);
            incomingToken.setTokenState(TokenState.WAITING);
        } else
            outgoingTokens = Token.diverge(processInstance, incomingToken, outgoing.size());

        for (int i = 0; i < outgoing.size(); i++)
            outgoingTokens.get(i).move(processInstance.getOutgoingSequenceFlows(this).get(i));

        return outgoingTokens;
    }
}
