package com.bpmnengine.model;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;

import java.util.LinkedList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class FlowNode extends TFlowNode {

    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        throw new UnsupportedOperationException();
    }

    protected List<Token> getTokensForNodeWaiting(List<Token> tokens, ProcessInstance processInstance) {
        List<Token> waiting = new LinkedList<>(tokens);
        for (Token token : processInstance.getTokens()) {
            if (token.getPosition().equals(this) && token.getTokenState().equals(TokenState.WAITING) && !waiting.contains(token)) {
                waiting.add(token);
            }
        }
        return waiting;
    }



    public boolean isActivity() {
        return false;
    }

    public Activity asActivity() {
        throw new IllegalStateException("This is not of type Activity");
    }

    public boolean isUserTask() {
        return false;
    }

    public UserTask asUserTask() {
        throw new IllegalStateException("This is not of type UserTask");
    }

    public boolean isCatchEvent() {
        return false;
    }

    public CatchEvent asCatchEvent() {
        throw new IllegalStateException("This is not of type CatchEvent");
    }

    public boolean isStartEvent() {
        return false;
    }

    public StartEvent asStartEvent() {
        throw new IllegalStateException("This is not of type StartEvent");
    }

    public boolean isScriptTask() {
        return false;
    }

    public ScriptTask asScriptTask() {
        throw new IllegalStateException("This is not of type ScriptTask");
    }

    public boolean isSubProcess() {
        return false;
    }

    public SubProcess asSubProcess() {
        throw new IllegalStateException("This is not of type SubProcess");
    }
}
