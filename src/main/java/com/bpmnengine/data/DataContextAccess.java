package com.bpmnengine.data;

import java.util.Map;

/**
 * @author André Waloszek
 */
public interface DataContextAccess {

    Map<String, Object> getDataInputs();

    Object getDataInput(String id);

    void setDataOutput(String id, Object value);

    Object getDataObject(String id);
}
