package com.bpmnengine.model;

import com.bpmnengine.ActivityInstance;
import com.bpmnengine.CompensationHandler;
import com.bpmnengine.LifecycleState;
import com.bpmnengine.ProcessInstance;
import com.bpmnengine.SubProcessInstance;
import com.bpmnengine.data.DataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TSubProcess;
import com.bpmnengine.token.Token;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;

/**
 * @author André Waloszek
 */
public class SubProcess extends TSubProcess {

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        tokens = getTokensForNodeWaiting(tokens, processInstance);

        ActivityInstance activityInstance = getOrCreateInactiveActivityInstance(processInstance);
        if (!(activityInstance instanceof ActivityInstance))
            throw new IllegalStateException();

        SubProcessInstance subProcessInstance = (SubProcessInstance) activityInstance;

        if (tokens.size() < getStartQuantity().intValue() && !isIsForCompensation())
            return Collections.emptyList();

        claimTokens(subProcessInstance, tokens);
        subProcessInstance.start();

        if (LifecycleState.COMPLETING.equals(subProcessInstance.getState()))
            return completing(activityInstance);

        return Collections.emptyList();
    }

    public List<Token> completing(ActivityInstance activityInstance) {
        if (!activityInstance.getState().equals(LifecycleState.COMPLETING))
            throw new IllegalStateException("Only SubProcesses in state COMPLETING can be completed");

        activityInstance.setState(LifecycleState.COMPLETED);

        ProcessInstance processInstance = activityInstance.getProcessInstance();
        CompensationHandler compensationHandler = processInstance.getCompensationHandler(this);
        if (compensationHandler != null)
            compensationHandler.addActivityInstanceToCompensate(activityInstance);

        return generateTokens(activityInstance);
    }


    @Override
    protected ActivityInstance createInactiveActivityInstance(ProcessInstance processInstance) {
        DataContext processDataContext = processInstance.getDataContext();
        DataContext dataContext = processDataContext.createDataContext(this);
        ActivityInstance instance = new SubProcessInstance(processInstance, this, dataContext);
        processInstance.addActivityInstance(instance);
        return instance;

    }



    @Override
    public boolean isSubProcess() {
        return true;
    }

    @Override
    public SubProcess asSubProcess() {
        return this;
    }

    public List<SequenceFlow> getSequenceFlows(List<QName> qNames) {
        return FlowElements.getSequenceFlows(getFlowElement(), qNames);
    }

    public List<TAssociation> getAssociations() {
        return Artifacts.getAssociations(getArtifact());
    }

    public List<TAssociation> getAssociations(String sourceRef) {
        return Artifacts.getAssociations(getArtifact(), sourceRef);
    }


}
