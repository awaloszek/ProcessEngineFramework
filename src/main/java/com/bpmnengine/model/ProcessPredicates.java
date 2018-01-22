package com.bpmnengine.model;

import com.bpmnengine.MessageInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TStartEvent;
import com.bpmnengine.lang.Predicate;

import static com.bpmnengine.lang.Predicates.and;
import static com.bpmnengine.lang.Predicates.isType;

/**
 * @author André Waloszek
 */
public class ProcessPredicates {

    public static Predicate isStart() {
        return isType(TStartEvent.class);
    }

    public static Predicate isCatchEvent() {
        return isType(IntermediateCatchEvent.class);
    }

    public static Predicate catches(MessageInstance messageInstance) {
        return and(isType(CatchEvent.class), catchesMessageEvent(messageInstance));
    }

    private static Predicate catchesMessageEvent(MessageInstance messageInstance) {
        return new CatchesMessageEvent(messageInstance);
    }

    private static class CatchesMessageEvent implements Predicate<CatchEvent> {

        private final MessageInstance messageInstance;

        private CatchesMessageEvent(MessageInstance messageInstance) {
            this.messageInstance = messageInstance;
        }

        @Override
        public boolean apply(CatchEvent catchEvent) {
            return catchEvent.catches(messageInstance);
        }
    }
}
