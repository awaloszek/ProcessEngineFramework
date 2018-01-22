package com.bpmnengine;

import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.ThrowEvent;
import com.bpmnengine.token.Token;
import com.bpmnengine.token.TokenState;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author André Waloszek
 */
public class CompensationHandler {

    public static CompensationHandler create(ProcessInstance processInstance, Activity activity) {
        QName activityRef = new QName("", activity.getId());

        TBoundaryEvent boundaryEvent = processInstance.getBoundaryEventAttachedToRef(activityRef);

        if (boundaryEvent == null || !boundaryEvent.hasCompensateEventDefinition())
            return null;

        return new CompensationHandler(processInstance, boundaryEvent);
    }

    private final ProcessInstance processInstance;

    private final List<ActivityInstance> activityInstancesToCompensate = new LinkedList<>();

    private final List<ActivityInstance> activityInstancesForCompensation = new LinkedList<>();

    private final TBoundaryEvent boundaryEvent;

    private final Map<ActivityInstance, ActivityInstance> toCompensateByForCompensation = new HashMap<>();

    private Token token;

    private ThrowEvent throwEvent;


    private CompensationHandler(ProcessInstance processInstance,
            TBoundaryEvent boundaryEvent) {
        this.processInstance = processInstance;
        this.boundaryEvent = boundaryEvent;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void compensate(ThrowEvent throwEvent, Token token) {
        this.throwEvent = throwEvent;
        this.token = token;
        List<Activity> activities = processInstance.getDefinitions().getExecutableProcess().getAssociatedActivities(boundaryEvent);

        boolean compensationRunning = false;
        for (ActivityInstance activityInstance : activityInstancesToCompensate) {
            if (activityInstance.getState().equals(LifecycleState.COMPENSATED) || activityInstance.getState().equals(LifecycleState.COMPENSATING)) {
                compensationRunning = true;
                break;
            }
        }

        if (compensationRunning) {
            token.setTokenState(TokenState.END);
            return;
        }

        for (ActivityInstance activityInstanceToCompensate : activityInstancesToCompensate) {
            if (LifecycleState.COMPENSATING.equals(activityInstanceToCompensate.getState()))
                continue;

            activityInstanceToCompensate.setState(LifecycleState.COMPENSATING);

            for (Activity activity : activities) {
                ActivityInstance activityInstance = activity.runForCompensation(this);
                activityInstancesForCompensation.add(activityInstance);
                toCompensateByForCompensation.put(activityInstance, activityInstanceToCompensate);
            }
        }
    }

    public void update() {
        if (throwEvent.getCompensateEventDefinition().isWaitForCompletion() && isCompensationDone()) {
            throwEvent.generateTokens(processInstance, token);
            processInstance.run();
        }
    }

    private boolean isCompensationDone() {
        for (ActivityInstance activityInstance : activityInstancesForCompensation) {
            if (!activityInstance.getState().equals(LifecycleState.COMPLETED))
                return false;
        }

        // TODO add detailed COMPENSATED check
        for (ActivityInstance activityInstance : activityInstancesToCompensate)
            activityInstance.setState(LifecycleState.COMPENSATED);

        return true;
    }

    public void addActivityInstanceToCompensate(ActivityInstance activityInstance) {
        activityInstancesToCompensate.add(activityInstance);
    }
}
