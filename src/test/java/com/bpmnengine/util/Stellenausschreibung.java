package com.bpmnengine.util;

import java.io.Serializable;

/**
 * @author André Waloszek
 */
public class Stellenausschreibung implements Serializable {

    private final long id;

    private State state = State.PROCESS;

    public Stellenausschreibung(Long id) {
        this.id = id;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
}