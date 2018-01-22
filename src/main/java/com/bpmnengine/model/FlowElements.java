package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TBoundaryEvent;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowElement;
import com.bpmnengine.lang.ListBuilder;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.bpmnengine.lang.Predicates.isType;

/**
 * @author André Waloszek
 */
public class FlowElements {

    public static List<FlowElement> getFlowElements(List<JAXBElement<? extends TFlowElement>> flowElements) {
        return ListBuilder.of(flowElements).transform(Functions.jaxbElementToValue()).toList();
    }

    public static List<FlowNode> getFlowNodes(List<JAXBElement<? extends TFlowElement>> flowElements) {
        return ListBuilder.of(flowElements).transform(Functions.jaxbElementToValue()).filter(isType(FlowNode.class)).toList();
    }

    public static List<SequenceFlow> getSequenceFlows(List<JAXBElement<? extends TFlowElement>> flowElements, List<QName> qNames) {
        List<String> ids = new ArrayList<>(qNames.size());
        for (QName qName : qNames)
            ids.add(qName.getLocalPart());

        List<SequenceFlow> sequenceFlows = new ArrayList<>(ids.size());

        for (FlowElement flowElement : getFlowElements(flowElements)) {
            if (flowElement.isSequenceFlow() && ids.contains(flowElement.getId())) {
                sequenceFlows.add(flowElement.asSequenceFlow());
            }
        }

        return sequenceFlows;
    }

    public static TBoundaryEvent getBoundaryEventAttachedToRef(List<JAXBElement<? extends TFlowElement>> flowElements, QName activityRef) {
        for (FlowElement flowElement : getFlowElements(flowElements)) {
            if (flowElement instanceof TBoundaryEvent) {
                TBoundaryEvent boundaryEvent = (TBoundaryEvent) flowElement;
                if (activityRef.equals(boundaryEvent.getAttachedToRef()))
                    return boundaryEvent;
            }
        }
        return null;
    }

    public static List<StartEvent> getStartEventWithoutEventDefinition(List<JAXBElement<? extends TFlowElement>> flowElements) {
        List<StartEvent> startEvents = new LinkedList<>();
        for (FlowNode flowNode : getFlowNodes(flowElements)) {
            if (flowNode.isStartEvent() && !flowNode.asStartEvent().hasEventDefinition())
                startEvents.add(flowNode.asStartEvent());
        }
        return startEvents;
    }

}
