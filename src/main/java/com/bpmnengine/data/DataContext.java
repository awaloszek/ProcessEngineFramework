package com.bpmnengine.data;

import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;

/**
 * @author André Waloszek
 */
public interface DataContext {

    void executeDataInputAssociations();

    void executeDataOutputAssociations();

    void setDataObject(String id, Object value);

    Object getDataObject(String id);

    DataContextAccess createDataContextAccess();

    DataContext createDataContext(Activity activity);

    CatchEventDataContext createDataContext(CatchEvent catchEvent);

}
