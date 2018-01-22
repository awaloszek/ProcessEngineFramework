package com.bpmnengine;

import com.bpmnengine.data.DataContextAccess;
import com.bpmnengine.model.UserTask;

/**
 * @author André Waloszek
 */
public class UserTaskInstance {

    private final ActivityInstance activityInstance;

    public UserTaskInstance(ActivityInstance activityInstance) {
        this.activityInstance = activityInstance;
    }

    public DataContextAccess getDataContext() {
        return activityInstance.getDataContext().createDataContextAccess();
    }

    public UserTask getUserTask() {
        return (UserTask) activityInstance.getActivity();
    }

    public ResourceInstance getResourceIntance() {
        return activityInstance.getResourceInstance();
    }

    public void completing() {
        activityInstance.getActivity().completing(activityInstance);
        ProcessInstance processInstance = activityInstance.getProcessInstance();
        processInstance.run();
    }
}
