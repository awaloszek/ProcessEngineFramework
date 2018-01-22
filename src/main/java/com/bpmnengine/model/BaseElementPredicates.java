package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TBaseElement;
import com.bpmnengine.lang.Predicate;

import java.util.List;
import java.util.Objects;

/**
 * @author André Waloszek
 */
public class BaseElementPredicates {

    public static Predicate<TBaseElement> withId(final String id) {
        return new Predicate<TBaseElement>() {
            @Override
            public boolean apply(TBaseElement value) {
                return Objects.equals(id, value.getId());
            }
        };
    }

    public static Predicate<TBaseElement> withId(final Predicate<String> predicate) {
        return new Predicate<TBaseElement>() {
            @Override
            public boolean apply(TBaseElement value) {
                return predicate.apply(value.getId());
            }
        };
    }

    public static Predicate in(final List elements) {
        return new Predicate() {
            @Override
            public boolean apply(Object value) {
                for (Object element : elements) {
                    if (Objects.equals(element, value)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
