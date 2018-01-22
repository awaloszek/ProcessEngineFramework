package com.bpmnengine.lang;

/**
 * @author André Waloszek
 */
public interface Function<Input, Output> {

    Output apply(Input input);
}
