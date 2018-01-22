package com.bpmnengine.lang;

/**
 * @author André Waloszek
 */
public interface Predicate<T> {

    boolean apply(T value);
}
