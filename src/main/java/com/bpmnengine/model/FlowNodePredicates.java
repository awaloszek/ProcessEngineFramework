package com.bpmnengine.model;

import com.bpmnengine.lang.Predicate;

/**
 * @author André Waloszek
 */
public final class FlowNodePredicates {

    public static Predicate<FlowNode> isUserTask() {
        return new Predicate<FlowNode>() {
            @Override
            public boolean apply(FlowNode value) {
                return value.isUserTask();
            }
        };
    }

}
