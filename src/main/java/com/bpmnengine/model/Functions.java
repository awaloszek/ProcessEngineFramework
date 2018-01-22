package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TEventDefinition;
import com.bpmnengine.lang.Function;

import javax.xml.bind.JAXBElement;

/**
 * @author André Waloszek
 */
public class Functions {

    public static Function<JAXBElement, TEventDefinition> jaxbElementToTEventDefinition() {
        return new Function<JAXBElement, TEventDefinition>() {
            @Override
            public TEventDefinition apply(JAXBElement jaxbElement) {
                return (TEventDefinition) jaxbElement.getValue();
            }
        };
    }

    public static Function<JAXBElement, Object> jaxbElementToValue() {
        return new Function<JAXBElement, Object>() {
            @Override
            public Object apply(JAXBElement jaxbElement) {
                return jaxbElement.getValue();
            }
        };
    }

    public static Function<Object, String> toStringList() {
        return new Function<Object, String>() {
            @Override
            public String apply(Object o) {
                return o.toString();
            }
        };
    }

}
