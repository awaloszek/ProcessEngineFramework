package com.bpmnengine;

/**
 * @author André Waloszek
 */
public class DefaultMessageInstance implements MessageInstance {

    private final String messageId;

    private final Object messageData;

    public DefaultMessageInstance(String messageId, Object messageData) {
        this.messageId = messageId;
        this.messageData = messageData;
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public Object getMessageData() {
        return messageData;
    }

}
