package com.bpmnengine.model;

import com.bpmnengine.MessageInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TEndEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowElement;
import com.bpmnengine.gen.omg.spec.bpmn.model.TProcess;
import com.bpmnengine.lang.Function;
import com.bpmnengine.lang.ListBuilder;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

import static com.bpmnengine.lang.Predicates.and;
import static com.bpmnengine.lang.Predicates.isType;
import static com.bpmnengine.model.FlowNodePredicates.isUserTask;

/**
 * @author André Waloszek
 */
public class Process extends TProcess {

    public List<FlowNode> getFlowNodes() {
        return ListBuilder.of(getFlowElement())
                .transform(toTFlowElement())
                .filter(isType(FlowNode.class))
                .toList();
    }

    public List<UserTask> getUserTasks() {
        return ListBuilder.of(getFlowNodes())
                .filter(isUserTask())
                .toList();
    }

    private static Function<JAXBElement, TFlowElement> toTFlowElement() {
        return new Function<JAXBElement, TFlowElement>() {
            @Override
            public TFlowElement apply(JAXBElement jaxbElement) {
                return (TFlowElement) jaxbElement.getValue();
            }
        };
    }

    public List<FlowElement> getFlowElements() {
        return ListBuilder.of(getFlowElement()).transform(Functions.jaxbElementToValue()).toList();
    }

    public List<StartEvent> getStartEventWithoutEventDefinition() {
        return FlowElements.getStartEventWithoutEventDefinition(getFlowElement());
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

    public FlowNode getAssociationTarget(TAssociation association) {
        for (FlowNode flowNode : getFlowNodes()) {
            if (association.getTargetRef().toString().equals(flowNode.getId()))
                return flowNode;
        }
        return null;
    }

    public List<Activity> getAssociatedActivities(TBoundaryEvent boundaryEvent) {
        List<TAssociation> associations = getAssociations(boundaryEvent.getId());
        List<Activity> activities = new LinkedList<>();

        for (TAssociation association : associations) {
            FlowNode flowNode = getAssociationTarget(association);
            if (flowNode.isActivity())
                activities.add(flowNode.asActivity());
        }

        return activities;
    }


    public List<CatchEvent> catches(MessageInstance messageInstance) {
        return ListBuilder.of(getFlowNodes())
                .filter(and(isType(CatchEvent.class), ProcessPredicates.catches(messageInstance)))
                .toList();
    }

    public List<StartEvent> catchesStart(MessageInstance messageInstance) {
        return ListBuilder.of(getFlowNodes())
                .filter(and(isType(StartEvent.class), ProcessPredicates.catches(messageInstance)))
                .toList();
    }

    public List<EndEvent> getEndEvents() {
        List<EndEvent> endEvents = new LinkedList<>();
        for (FlowNode flowNode : getFlowNodes()) {
            if (flowNode instanceof EndEvent)
                endEvents.add((EndEvent) flowNode);
        }
        return endEvents;
    }
}
