package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.data.DataContext;
import com.bpmnengine.token.Token;
import com.bpmnengine.gen.omg.spec.bpmn.model.TExclusiveGateway;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class ExclusiveGateway extends TExclusiveGateway {

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> waiting = getTokensForNodeWaiting(tokens, processInstance);

        List<Token> moved = new LinkedList<>();
        SequenceFlow defaultFlow = (SequenceFlow) getDefault();

        for (Token token : waiting) {
            DataContext processDataContext = processInstance.getDataContext();
            List<SequenceFlow> outgoing = processInstance.getOutgoingSequenceFlows(token.getPosition());
            outgoing.remove(defaultFlow);
            Token movedToken = null;

            for (SequenceFlow sequenceFlow : outgoing) {
                if (sequenceFlow.evalCondition(processDataContext.createDataContextAccess())) {
                    token.move(sequenceFlow);
                    movedToken = token;
                    break;
                }
            }

            if (movedToken == null && defaultFlow != null) {
                token.move(defaultFlow);
                movedToken = token;
            }

            if (movedToken == null)
                throw new IllegalStateException();

            moved.add(movedToken);
        }

        return moved;
    }
}
