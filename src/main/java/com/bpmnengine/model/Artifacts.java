package com.bpmnengine.model;

import com.bpmnengine.gen.omg.spec.bpmn.model.TArtifact;
import com.bpmnengine.gen.omg.spec.bpmn.model.TAssociation;

import javax.xml.bind.JAXBElement;
import java.util.LinkedList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class Artifacts {

    public static List<TAssociation> getAssociations(List<JAXBElement<? extends TArtifact>> artifacts) {
        List<TAssociation> associations = new LinkedList<>();
        for (JAXBElement<? extends TArtifact> jaxbElement : artifacts) {
            TArtifact artifact = jaxbElement.getValue();
            if (artifact instanceof TAssociation) {
                associations.add((TAssociation) artifact);
            }
        }
        return associations;
    }

    public static List<TAssociation> getAssociations(List<JAXBElement<? extends TArtifact>> artifacts, String sourceRef) {
        List<TAssociation> associations = new LinkedList<>();
        for (TAssociation association : getAssociations(artifacts)) {
            if (association.getSourceRef().toString().equals(sourceRef))
                associations.add(association);
        }
        return associations;
    }

}
