package com.bpmnengine.util;

import java.io.Serializable;

/**
 * @author André Waloszek
 */
public enum State implements Serializable {

    PROCESS,

    CHECK,

    APPROVED,

    EXECUTE,

    DONE
}