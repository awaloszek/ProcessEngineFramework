package com.bpmnengine.data;

import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObject;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowElement;
import com.bpmnengine.gen.omg.spec.bpmn.model.TInputOutputSpecification;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.SubProcess;

import javax.xml.bind.JAXBElement;
import java.util.List;

/**
 * @author André Waloszek
 */
public class ActivityDataContext extends AbstractDataContext {

    private final Activity activity;

    public ActivityDataContext(DataContext parent, Activity activity) {
        super(parent);
        this.activity = activity;

        if (activity instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) activity;
            for (JAXBElement<? extends TFlowElement> jaxbElement : subProcess.getFlowElement()) {
                TFlowElement value = jaxbElement.getValue();

                if (value instanceof TDataObject) {
                    data.put(value.getId(), new EmptyValue());
                }
            }
        }
    }

    @Override
    protected TInputOutputSpecification getIoSpecification() {
        return activity.getIoSpecification();
    }

    @Override
    protected List<TDataInputAssociation> getDataInputAssociation() {
        return activity.getDataInputAssociation();
    }

    @Override
    protected List<TDataOutputAssociation> getDataOutputAssociation() {
        return activity.getDataOutputAssociation();
    }

    @Override
    public DataContext createDataContext(Activity activity) {
        return new ActivityDataContext(this, activity);
    }

    @Override
    public CatchEventDataContext createDataContext(CatchEvent catchEvent) {
        return new CatchEventDataContext(this, catchEvent);
    }
}
