package com.bpmnengine;

import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.token.Token;

/**
 * @author André Waloszek
 */
public class CatchEventTrigger {

    private final CatchEvent catchEvent;

    private final Token token;

    public CatchEventTrigger(CatchEvent catchEvent, Token token) {
        this.catchEvent = catchEvent;
        this.token = token;
    }

    public CatchEvent getCatchEvent() {
        return catchEvent;
    }

    public Token getToken() {
        return token;
    }
}
