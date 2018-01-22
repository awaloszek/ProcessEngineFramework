package com.bpmnengine.token;

/**
 * @author André Waloszek
 */
public enum TokenState {

    WAITING,

    WAITING_FOR_COMPLETION,

    DIVERGED,

    CONVERGED,

    ACTIVITY,

    WAITING_FOR_TRIGGER,

    END
}
