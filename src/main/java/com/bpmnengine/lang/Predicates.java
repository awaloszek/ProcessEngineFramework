package com.bpmnengine.lang;

import java.util.List;

/**
 * @author André Waloszek
 */
public class Predicates {

    public static Predicate and(Predicate... predicates) {
        return new And(predicates);
    }

    public static Predicate isType(Class clazz) {
        return new IsType(clazz);
    }

    public static Predicate not(Predicate predicate) {
        return new Not(predicate);
    }

    public static Predicate exclude(List list) {
        return new Exclude(list);
    }

    private static class Exclude implements Predicate {

        private final List list;

        private Exclude(List list) {
            this.list = list;
        }

        @Override
        public boolean apply(Object value) {
            return !list.contains(value);
        }
    }

    private static class IsType implements Predicate {

        private final Class clazz;

        public IsType(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean apply(Object value) {
            return clazz.isInstance(value);
        }
    }

    private static class And implements Predicate {

        private final Predicate[] predicates;

        And(Predicate... predicates) {
            this.predicates = predicates;
        }

        @Override
        public boolean apply(Object value) {
            for (Predicate predicate : predicates) {
                if (!predicate.apply(value))
                    return false;
            }
            return true;
        }
    }

    private static class Not implements Predicate {

        private final Predicate predicate;

        private Not(Predicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean apply(Object value) {
            return !predicate.apply(value);
        }
    }
}
