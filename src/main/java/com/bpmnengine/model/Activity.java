package com.bpmnengine.model;

import com.bpmnengine.ActivityInstance;
import com.bpmnengine.CompensationHandler;
import com.bpmnengine.LifecycleState;
import com.bpmnengine.ProcessInstance;
import com.bpmnengine.ResourceInstance;
import com.bpmnengine.data.DataContext;
import com.bpmnengine.gen.omg.spec.bpmn.model.TActivity;
import com.bpmnengine.gen.omg.spec.bpmn.model.TResourceRole;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class Activity extends TActivity {

    @Override
    public boolean isActivity() {
        return true;
    }

    @Override
    public Activity asActivity() {
        return this;
    }

    @Override
    public List<Token> pass(List<Token> tokens, ProcessInstance processInstance) {
        tokens = getTokensForNodeWaiting(tokens, processInstance);

        ActivityInstance activityInstance = getOrCreateInactiveActivityInstance(processInstance);

        if (tokens.size() < getStartQuantity().intValue() && !isIsForCompensation())
            return Collections.emptyList();

        claimTokens(activityInstance, tokens);
        executeDataInputAssociations(activityInstance);
        bindResource(activityInstance);
        LifecycleState state = run(activityInstance);

        if (LifecycleState.COMPLETING.equals(state))
            return completing(activityInstance);

        return Collections.emptyList();
    }

    public ActivityInstance runForCompensation(CompensationHandler compensationHandler) {
        if (!isIsForCompensation())
            throw new IllegalStateException("Activity must be for compensation to run without token");

        ActivityInstance activityInstance = getOrCreateInactiveActivityInstance(compensationHandler.getProcessInstance());
        activityInstance.setCompensationHandler(compensationHandler);
        executeDataInputAssociations(activityInstance);
        bindResource(activityInstance);
        LifecycleState state = run(activityInstance);

        if (LifecycleState.COMPLETING.equals(state))
            completing(activityInstance);

        return activityInstance;
    }

    private void executeDataInputAssociations(ActivityInstance activityInstance) {
        activityInstance.setState(LifecycleState.READY);
        activityInstance.getDataContext().executeDataInputAssociations();
        activityInstance.setState(LifecycleState.ACTIVE);
    }

    protected ActivityInstance getOrCreateInactiveActivityInstance(ProcessInstance processInstance) {
        ActivityInstance activityInstance = getInactiveActivityInstance(processInstance);
        return activityInstance == null ? createInactiveActivityInstance(processInstance) : activityInstance;
    }

    public List<Token> completing(ActivityInstance activityInstance) {
        if (!activityInstance.getState().equals(LifecycleState.ACTIVE))
            throw new IllegalStateException("Only Activities in state ACTIVE can be completed");

        executeDataOutputAssociations(activityInstance);
        if (isIsForCompensation()) {
            activityInstance.getCompensationHandler().update();
            return Collections.emptyList();

        } else {
            ProcessInstance processInstance = activityInstance.getProcessInstance();
            CompensationHandler compensationHandler = processInstance.getCompensationHandler(this);
            if (compensationHandler != null)
                compensationHandler.addActivityInstanceToCompensate(activityInstance);
        }

        return generateTokens(activityInstance);
    }

    private void executeDataOutputAssociations(ActivityInstance activityInstance) {
        activityInstance.setState(LifecycleState.COMPLETING);
        activityInstance.getDataContext().executeDataOutputAssociations();
        activityInstance.setState(LifecycleState.COMPLETED);
    }

    protected List<Token> generateTokens(ActivityInstance activityInstance) {
        ProcessInstance processInstance = activityInstance.getProcessInstance();
        SequenceFlow defaultFlow = (SequenceFlow) activityInstance.getActivity().getDefault();
        List<SequenceFlow> outgoing = processInstance.getOutgoingSequenceFlows(this);
        outgoing.remove(defaultFlow);
        DataContext processDataContext = processInstance.getDataContext();

        Token token;
        if (activityInstance.getClaimedTokens().size() == 1) {
            token = activityInstance.getClaimedTokens().get(0);
            token.setTokenState(TokenState.WAITING);
        } else
            token = Token.converge(processInstance, activityInstance.getClaimedTokens());

        List<SequenceFlow> openOutgoings = new LinkedList<>();
        for (SequenceFlow sequenceFlow : outgoing) {
            if (sequenceFlow.evalCondition(processDataContext.createDataContextAccess())) {
                openOutgoings.add(sequenceFlow);
            }
        }

        if (openOutgoings.isEmpty())
            openOutgoings.add(defaultFlow);

        if (openOutgoings.isEmpty())
            throw new IllegalStateException();

        List<Token> outgoingTokens = openOutgoings.size() == 1 ? Collections.singletonList(token) : Token.diverge(processInstance, token, openOutgoings.size());
        for (int i = 0; i < openOutgoings.size(); i++)
            outgoingTokens.get(i).move(openOutgoings.get(i));
        return outgoingTokens;
    }

    protected LifecycleState run(ActivityInstance activityInstance) {
        throw new UnsupportedOperationException();
    }

    private void bindResource(ActivityInstance activityInstance) {
        for (JAXBElement<? extends TResourceRole> jaxbElement : getResourceRole()) {
            TResourceRole value = jaxbElement.getValue();

            if (!(value instanceof PotentialOwner))
                throw new IllegalStateException("Only PotentialOwner ResourceRole supported");

            PotentialOwner potentialOwner = (PotentialOwner) value;
            ResourceInstance resourceInstance = potentialOwner.eval(activityInstance);
            activityInstance.setResourceInstance(resourceInstance);
        }
    }

    protected void claimTokens(ActivityInstance activityInstance, List<Token> waitingTokens) {
        List<Token> claimedTokens = new ArrayList<>(getStartQuantity().intValue());
        for (int i = 0; i < getStartQuantity().intValue(); i++) {
            Token token = waitingTokens.get(i);
            token.setTokenState(TokenState.ACTIVITY);
            claimedTokens.add(token);
        }
        activityInstance.setClaimedTokens(claimedTokens);
    }

    protected ActivityInstance getInactiveActivityInstance(ProcessInstance processInstance) {
        for (ActivityInstance activityInstance : processInstance.getActivityInstances()) {
            if (LifecycleState.INACTIVE.equals(activityInstance.getState())) {
                return activityInstance;
            }
        }
        return null;
    }

    protected ActivityInstance createInactiveActivityInstance(ProcessInstance processInstance) {
        DataContext processDataContext = processInstance.getDataContext();
        DataContext dataContext = processDataContext.createDataContext(this);
        ActivityInstance instance = new ActivityInstance(processInstance, this, dataContext);
        processInstance.addActivityInstance(instance);
        return instance;
    }
}
