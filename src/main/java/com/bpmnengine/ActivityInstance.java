package com.bpmnengine;

import com.bpmnengine.data.DataContext;
import com.bpmnengine.model.Activity;
import com.bpmnengine.token.Token;

import java.util.List;

/**
 * @author André Waloszek
 */
public class ActivityInstance {

    protected final ProcessInstance processInstance;

    protected final Activity activity;

    protected final DataContext dataContext;

    private LifecycleState state = LifecycleState.INACTIVE;

    private ResourceInstance resourceInstance;

    private List<Token> claimedTokens;

    private CompensationHandler compensationHandler;

    public ActivityInstance(ProcessInstance processInstance, Activity activity, DataContext dataContext) {
        this.processInstance = processInstance;
        this.activity = activity;
        this.dataContext = dataContext;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public CompensationHandler getCompensationHandler() {
        return compensationHandler;
    }

    public void setCompensationHandler(CompensationHandler compensationHandler) {
        this.compensationHandler = compensationHandler;
    }

    public Activity getActivity() {
        return activity;
    }

    public LifecycleState getState() {
        return state;
    }

    public void setState(LifecycleState lifecycleState) {
        this.state = lifecycleState;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public ResourceInstance getResourceInstance() {
        return resourceInstance;
    }

    public void setResourceInstance(ResourceInstance resourceInstance) {
        this.resourceInstance = resourceInstance;
    }

    public void setClaimedTokens(List<Token> claimedTokens) {
        this.claimedTokens = claimedTokens;
    }

    public List<Token> getClaimedTokens() {
        return claimedTokens;
    }
}
