package com.bpmnengine.model;

import com.bpmnengine.CorrelationKeyInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TCorrelationKey;
import com.bpmnengine.gen.omg.spec.bpmn.model.TMessage;
import com.bpmnengine.gen.omg.spec.bpmn.model.TRootElement;
import com.bpmnengine.lang.Function;
import com.bpmnengine.lang.ListBuilder;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bpmnengine.lang.Predicates.and;
import static com.bpmnengine.lang.Predicates.isType;
import static com.bpmnengine.model.BaseElementPredicates.in;
import static com.bpmnengine.model.BaseElementPredicates.withId;

/**
 * @author André Waloszek
 */
public abstract class Definitions {

    public abstract List<JAXBElement<? extends TRootElement>> getRootElement();

    public TMessage getMessage(QName name) {
        return (TMessage) ListBuilder.of(getRootElement())
                .transform(toRootElement())
                .filter(and(isType(TMessage.class), withId(name.toString())))
                .toElement();
    }

    public List<CorrelationProperty> getCorrelationProperties() {
        return ListBuilder.of(getRootElements())
                .filter(isType(CorrelationProperty.class))
                .toList();
    }

    public List<CorrelationProperty> getCorrelationProperties(CorrelationKey correlationKey) {
        return ListBuilder.of(getCorrelationProperties())
                .filter(withId(in(correlationKey.getCorrelationPropertyIds())))
                .toList();
    }

    private List getRootElements() {
        return ListBuilder.of(getRootElement()).transform(toRootElement()).toList();
    }

    public Collaboration getCollaboration() {
        Collaboration collaboration = (Collaboration) ListBuilder.of(getRootElements())
                .filter(isType(Collaboration.class))
                .toElement();
        if (collaboration == null)
            throw new IllegalStateException("Model must have a collaboration.");

        return collaboration;
    }

    public List<Process> getProcesses() {
        return ListBuilder.of(getRootElements()).filter(isType(Process.class)).toList();
    }

    public Process getProcess(String id) {
        for (Process process : getProcesses()) {
            if (id.equals(process.getId()))
                return process;
        }

        throw new IllegalStateException("Process with id " + id + " not found.");
    }

    public Process getExecutableProcess() {
        Process processCandidate = null;

        for (Process process : getProcesses()) {
            if (process.isIsExecutable()) {
                if (processCandidate != null)
                    throw new IllegalStateException("Only one executable process allowed in model");

                processCandidate = process;
            }
        }

        if (processCandidate == null)
            throw new IllegalStateException("No executable process found.");

        return processCandidate;

    }

    private static Function<JAXBElement, TRootElement> toRootElement() {
        return new Function<JAXBElement, TRootElement>() {
            @Override
            public TRootElement apply(JAXBElement jaxbElement) {
                return (TRootElement) jaxbElement.getValue();
            }
        };
    }

    public CorrelationKeyInstance createCorrelationKeyInstance(TCorrelationKey key) {
        Map<String, Object> properties = new HashMap<>();

        for (CorrelationProperty property : getCorrelationProperties()) {
            properties.put(property.getId(), null);
        }

        return new CorrelationKeyInstance(key, properties);
    }

}
