package com.bpmnengine;

import java.io.Serializable;

/**
 * @author André Waloszek
 */
public class MessagePayload implements Serializable{

    private boolean isEither = true;

    private String key = "defaultKey";

    private String state = "open";

    private String user = "TestUser";

    private String group = "TestGroup";

    private int intValue = 0;

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public boolean isEither() {
        return isEither;
    }

    public void setEither(boolean value) {
        this.isEither = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return "This message is " + ((isEither) ? "Either" : "Or");
    }
}
