package com.bpmnengine.data;

import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInput;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObject;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutput;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TInputOutputSpecification;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.FlowElement;
import com.bpmnengine.model.Process;

import java.util.List;

/**
 * @author André Waloszek
 */
public class ProcessDataContext extends AbstractDataContext {

    private final Process process;

    public ProcessDataContext(Process process) {
        super(null);
        this.process = process;

        for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof TDataObject) {
                data.put(flowElement.getId(), new EmptyValue());
            }
        }

        TInputOutputSpecification ioSpecification = process.getIoSpecification();
        if (ioSpecification != null) {
            for (TDataInput dataInput : ioSpecification.getDataInput())
                data.put(dataInput.getId(), new EmptyValue());
            for (TDataOutput dataOutput : ioSpecification.getDataOutput())
                data.put(dataOutput.getId(), new EmptyValue());
        }
    }

    @Override
    protected TInputOutputSpecification getIoSpecification() {
        return process.getIoSpecification();
    }

    @Override
    protected List<TDataInputAssociation> getDataInputAssociation() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<TDataOutputAssociation> getDataOutputAssociation() {
        throw new UnsupportedOperationException();
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
