package com.bpmnengine;

/**
 * @author André Waloszek
 */
public interface MessageInstance {

    String getMessageId();

    Object getMessageData();
}
