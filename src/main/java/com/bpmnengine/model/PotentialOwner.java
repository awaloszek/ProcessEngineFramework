package com.bpmnengine.model;

import com.bpmnengine.ActivityInstance;
import com.bpmnengine.ResourceInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TExpression;
import com.bpmnengine.gen.omg.spec.bpmn.model.TPotentialOwner;
import com.bpmnengine.gen.omg.spec.bpmn.model.TResourceParameterBinding;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author André Waloszek
 */
public class PotentialOwner extends TPotentialOwner {

    public ResourceInstance eval(ActivityInstance activityInstance) {
        Map<String, Object> properties = new HashMap<>();

        for (TResourceParameterBinding resourceParameterBinding : getResourceParameterBinding()) {
            JAXBElement<? extends TExpression> jaxbElement = resourceParameterBinding.getExpression();
            TExpression value = jaxbElement.getValue();

            if (!(value instanceof FormalExpression))
                throw new IllegalStateException("Only FormalExpression is supported for Resource Parameter Binding evaluation");

            FormalExpression formalExpression = (FormalExpression) value;
            Object result = formalExpression.eval(activityInstance.getDataContext().createDataContextAccess());

            properties.put(resourceParameterBinding.getParameterRef().toString(), result);

        }

        return new ResourceInstance(getResourceRef().toString(), properties);
    }
}
