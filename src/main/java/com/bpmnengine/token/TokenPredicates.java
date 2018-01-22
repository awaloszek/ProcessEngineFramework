package com.bpmnengine.token;

import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;
import com.bpmnengine.lang.Predicate;

/**
 * @author André Waloszek
 */
public class TokenPredicates {

    public static Predicate isPosition(TFlowNode flowNode) {
        return new IsPosition(flowNode);
    }

    public static Predicate isNotWaiting() {
        return new IsNotWaiting();
    }

    private static class IsPosition implements Predicate<Token> {

        private final TFlowNode position;

        private IsPosition(TFlowNode position) {
            this.position = position;
        }

        @Override
        public boolean apply(Token value) {
            return value.getPosition().getId().equals(position.getId());
        }
    }

    private static class IsNotWaiting implements Predicate<Token> {

        @Override
        public boolean apply(Token value) {
            return !value.getTokenState().equals(TokenState.WAITING);
        }
    }

}
