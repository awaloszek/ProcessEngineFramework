package com.bpmnengine.token;

import com.bpmnengine.ProcessInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TSequenceFlow;
import com.bpmnengine.model.FlowNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author André Waloszek
 */
public class Token {

    private static String createId() {
        return UUID.randomUUID().toString();
    }

    public static Token create(ProcessInstance processInstance, FlowNode node) {
        return new Token(processInstance, createId(), node);
    }

    public static List<Token> create(ProcessInstance processInstance, List<? extends FlowNode> nodes) {
        List<Token> tokens = new ArrayList<>(nodes.size());
        for (FlowNode node : nodes) {
            tokens.add(new Token(processInstance, createId(), node));
        }
        return tokens;
    }

    public static List<Token> diverge(ProcessInstance processInstance, Token token, int amount) {
        token.setTokenState(TokenState.DIVERGED);
        List<Token> tokens = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            tokens.add(new Token(processInstance, createId(), token.position, token));
        }
        return tokens;
    }

    public static Token converge(ProcessInstance processInstance, List<Token> tokens) {
        for (Token token : tokens) {
            token.setTokenState(TokenState.CONVERGED);
        }
        return new Token(processInstance, createId(), tokens.get(0).position, tokens);
    }



    private final String id;

    private final List<Token> parents = new ArrayList<>();

    private final List<String> path = new ArrayList<>();

    private TokenState tokenState = TokenState.WAITING;

    private FlowNode position;


    private Token(ProcessInstance processInstance, String id, FlowNode node) {
        this(processInstance, id, node, Collections.<Token>emptyList());
    }

    private Token(ProcessInstance processInstance, String id, FlowNode position, Token parent) {
        this(processInstance, id, position, Arrays.asList(parent));
    }

    private Token(ProcessInstance processInstance, String id, FlowNode position, List<Token> parents) {
        this.id = id;
        this.parents.addAll(parents);
        this.position = position;
        path.add(position.getId());
        processInstance.addToken(this);
    }

    public TokenState getTokenState() {
        return tokenState;
    }

    public void setTokenState(TokenState tokenState) {
        this.tokenState = tokenState;
    }

    public FlowNode getPosition() {
        return position;
    }

    public void move(TSequenceFlow sequenceFlow) {
        if (!tokenState.equals(TokenState.WAITING))
            throw new IllegalStateException("Only waiting Tokens can be moved");

        FlowNode position = (FlowNode) sequenceFlow.getTargetRef();
        this.path.add(sequenceFlow.getId());
        this.path.add(position.getId());
        this.position = position;
    }

    public String getIncomingSequenceFlowId() {
        return path.get(path.size() - 2);
    }

    public boolean isAlive() {
        return tokenState.equals(TokenState.WAITING) || tokenState.equals(TokenState.ACTIVITY) || tokenState.equals(TokenState.WAITING_FOR_TRIGGER);
    }

    public List<String> getPath() {
        return new ArrayList<>(path);
    }

    public List<Token> getParents() {
        return new ArrayList<>(parents);
    }

    public String getPathAsString() {
        String value = "";
        for (String element : path)
            value += element + ".";
        return value.substring(0, value.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(id, token.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
