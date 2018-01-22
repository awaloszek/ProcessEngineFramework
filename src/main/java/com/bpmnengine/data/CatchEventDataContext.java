package com.bpmnengine.data;

import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObject;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObjectReference;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutput;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TInputOutputSpecification;
import com.bpmnengine.model.Activity;
import com.bpmnengine.model.CatchEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author André Waloszek
 */
public class CatchEventDataContext extends AbstractDataContext {

    private final CatchEvent catchEvent;

    public CatchEventDataContext(DataContext parent, CatchEvent catchEvent) {
        super(parent);
        this.catchEvent = catchEvent;
    }

    
    public void execute(Object data) {
        fillDataOutput(data);
        executeDataOutputAssociations();
    }


    private void fillDataOutput(Object data) {
        List<TDataOutput> dataOutputs = catchEvent.getDataOutput();

        if (dataOutputs.size() > 1)
            throw new IllegalArgumentException("There must only be one DataOutput");

        for (TDataOutput dataOutput : dataOutputs) {
            this.data.put(dataOutput.getId(), data);
        }
    }

    @Override
    protected List<TDataOutputAssociation> getDataOutputAssociation() {
        return catchEvent.getDataOutputAssociation();
    }

    @Override
    protected boolean hasIoSpecification() {
        return false;
    }

    @Override
    public void executeDataInputAssociations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataContext createDataContext(Activity activity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CatchEventDataContext createDataContext(CatchEvent catchEvent) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TInputOutputSpecification getIoSpecification() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<TDataInputAssociation> getDataInputAssociation() {
        throw new UnsupportedOperationException();
    }

}
