package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TCorrelationProperty;
import com.bpmnengine.lang.ListBuilder;
import com.bpmnengine.lang.Predicate;

import java.util.Objects;

/**
 * @author André Waloszek
 */
public class CorrelationProperty extends TCorrelationProperty {

    public CorrelationPropertyRetrievalExpression getCorrelationPropertyRetrievelExpression(String messageId) {
        return (CorrelationPropertyRetrievalExpression) ListBuilder.of(getCorrelationPropertyRetrievalExpression())
                .filter(byMessageId(messageId))
                .toElement();
    }

    private static Predicate<CorrelationPropertyRetrievalExpression> byMessageId(final String messageId) {
        return new Predicate<CorrelationPropertyRetrievalExpression>() {
            @Override
            public boolean apply(CorrelationPropertyRetrievalExpression value) {
                return Objects.equals(value.getMessageId(), messageId);
            }
        };
    }
}
