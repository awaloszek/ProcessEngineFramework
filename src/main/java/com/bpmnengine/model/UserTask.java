package com.bpmnengine.model;

import com.bpmnengine.ActivityInstance;
import com.bpmnengine.LifecycleState;
import com.bpmnengine.gen.omg.spec.bpmn.model.TUserTask;

/**
 * @author André Waloszek
 */
public class UserTask extends TUserTask {

    @Override
    public boolean isUserTask() {
        return true;
    }

    @Override
    public UserTask asUserTask() {
        return this;
    }

    @Override
    protected LifecycleState run(ActivityInstance activityInstance) {
        return LifecycleState.ACTIVE;
    }
}
