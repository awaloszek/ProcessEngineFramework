package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.token.Token;
import com.bpmnengine.gen.omg.spec.bpmn.model.TParallelGateway;
import com.bpmnengine.gen.omg.spec.bpmn.model.TSequenceFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author André Waloszek
 */
public class ParallelGateway extends TParallelGateway {

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> waitingTokens = getTokensForNodeWaiting(tokens, processInstance);

        List<SequenceFlow> incoming = processInstance.getIncomingSequenceFlows(this);
        List<Token> incomingTokens = new ArrayList<>(incoming.size());
        Token token;

        if (incoming.size() > 1) {
            for (TSequenceFlow sequenceFlow : incoming) {
                Token incomingTokenFound = null;
                for (Token incomingToken : waitingTokens) {
                    if (incomingToken.getIncomingSequenceFlowId().equals(sequenceFlow.getId())) {
                        incomingTokenFound = incomingToken;
                        break;
                    }
                }

                if (incomingTokenFound == null)
                    return Collections.emptyList();

                incomingTokens.add(incomingTokenFound);
            }

            token = Token.converge(processInstance, incomingTokens);

        } else
            token = waitingTokens.get(0);

        List<Token> outgoingTokens = outgoing.size() == 1 ? Collections.singletonList(token) : Token.diverge(processInstance, token, outgoing.size());
        for (int i = 0; i < outgoing.size(); i++)
            outgoingTokens.get(i).move(processInstance.getOutgoingSequenceFlows(this).get(i));

        return outgoingTokens;
    }
}
