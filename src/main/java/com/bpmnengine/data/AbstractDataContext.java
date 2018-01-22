package com.bpmnengine.data;

import com.bpmnengine.gen.omg.spec.bpmn.model.TBaseElement;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInput;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataInputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObject;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataObjectReference;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutput;
import com.bpmnengine.gen.omg.spec.bpmn.model.TDataOutputAssociation;
import com.bpmnengine.gen.omg.spec.bpmn.model.TInputOutputSpecification;
import com.bpmnengine.gen.omg.spec.bpmn.model.TInputSet;
import com.bpmnengine.gen.omg.spec.bpmn.model.TOutputSet;
import com.bpmnengine.lang.Lists;

import javax.xml.bind.JAXBElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bpmnengine.model.BaseElementPredicates.withId;

/**
 * @author André Waloszek
 */
public abstract class AbstractDataContext implements DataContext {

    protected final DataContext parent;

    protected final Map<String, Object> data = new HashMap<>();

    public AbstractDataContext(DataContext parent) {
        this.parent = parent;
    }

    protected boolean hasIoSpecification() {
        return true;
    }

    protected abstract TInputOutputSpecification getIoSpecification();

    protected abstract List<TDataInputAssociation> getDataInputAssociation();

    protected abstract List<TDataOutputAssociation> getDataOutputAssociation();

    public void setDataObject(String id, Object value) {
        if (data.containsKey(id))
            data.put(id, value);
        else if (parent != null)
            parent.setDataObject(id, value);
        else
            throw new IllegalArgumentException("DataObject with id " + id + " does not exist");
    }

    public void executeDataInputAssociations() {
        for (TDataInputAssociation dataInputAssociation : getDataInputAssociation()) {
            TBaseElement source = (TBaseElement) dataInputAssociation.getSourceRef().get(0).getValue();

            if (source instanceof TDataObjectReference) {
                TDataObjectReference dataObjectReference = (TDataObjectReference) source;
                source = (TDataObject) dataObjectReference.getDataObjectRef();
            }

            if (!(source instanceof TDataInput || source instanceof TDataObject))
                throw new IllegalStateException("Unsupported type " + source.getClass().getName());

            TDataInput dataInput = (TDataInput) dataInputAssociation.getTargetRef();
            data.put(dataInput.getId(), getDataObject(source.getId()));
        }

        if (!hasIoSpecification())
            return;

        TInputOutputSpecification ioSpecification = getIoSpecification();

        if (ioSpecification != null && ioSpecification.getInputSet().size() > 0) {
            TInputSet inputSet = getValidInputSet();
            if (inputSet == null)
                throw new IllegalStateException("No valid InputSet found");
        }
    }

    private TInputSet getValidInputSet() {
        for (TInputSet inputSet : getIoSpecification().getInputSet()) {
            boolean valid = true;
            for (JAXBElement<Object> dataInputRef : inputSet.getDataInputRefs()) {
                TDataInput dataInput = (TDataInput) dataInputRef.getValue();

                if (data.get(dataInput.getId()) == null) {
                    valid = false;
                    break;
                }
            }
            if (valid)
                return inputSet;
        }
        return null;
    }

    private boolean hasOutputSet() {
        return getIoSpecification() != null
                && getIoSpecification().getOutputSet() != null
                && getIoSpecification().getOutputSet().size() > 0;
    }

    private TOutputSet getValidOutputSet() {
        for (TOutputSet outputSet : getIoSpecification().getOutputSet()) {
            boolean valid = true;

            for (JAXBElement<Object> dataOutputRef : outputSet.getDataOutputRefs()) {
                TDataOutput dataOutput = (TDataOutput) dataOutputRef.getValue();

                if (data.get(dataOutput.getId()) == null) {
                    valid = false;
                    break;
                }
            }
            if (valid)
                return outputSet;
        }
        return null;
    }


    private void setDataOutput(String id, Object data) {
        TDataOutput dataOutput = (TDataOutput) Lists.first(getIoSpecification().getDataOutput(), withId(id));
        if (dataOutput == null)
            throw new IllegalArgumentException("DataOutput [" + id + "] not available");

        this.data.put(id, data);
    }

    private Map<String, Object> getDataInputs() {
        if (!hasIoSpecification())
            throw new UnsupportedOperationException();

        Map<String, Object> dataInputs = new HashMap<>();
        for (TDataInput dataInput : getIoSpecification().getDataInput()) {
            dataInputs.put(dataInput.getId(), getDataInput(dataInput.getId()));
        }
        return dataInputs;
    }

    private Object getDataInput(String id) {
        TDataInput dataInput = (TDataInput) Lists.first(getIoSpecification().getDataInput(), withId(id));
        if (dataInput == null)
            throw new IllegalArgumentException("DataInput not available");

        return data != null ? Data.copy(data.get(id)) : data;
    }

    public Object getDataObject(String id) {
        Object value = data.get(id);
        if (value == null && parent != null)
            value = parent.getDataObject(id);
        return value != null ? Data.copy(value) : null;
    }

    public void executeDataOutputAssociations() {
        if (hasIoSpecification() && hasOutputSet() && getValidOutputSet() == null)
            throw new IllegalStateException("No valid OutputSet available");

        for (TDataOutputAssociation dataOutputAssociation : getDataOutputAssociation()) {
            TDataOutput dataOutput = (TDataOutput) dataOutputAssociation.getSourceRef().get(0).getValue();

            Object target = dataOutputAssociation.getTargetRef();
            if (target instanceof TDataObjectReference) {
                TDataObjectReference dataObjectReference = (TDataObjectReference) target;
                TDataObject dataObject = (TDataObject) dataObjectReference.getDataObjectRef();
                Object object = data.remove(dataOutput.getId());
                setDataObject(dataObject.getId(), object);

            } else if (target instanceof TDataObject) {
                TDataObject dataObject = (TDataObject) target;
                Object object = data.remove(dataOutput.getId());
                setDataObject(dataObject.getId(), object);

            } else if (target instanceof TDataInput) {
                TDataInput dataInput = (TDataInput) target;
                Object object = data.remove(dataOutput.getId());
                setDataObject(dataInput.getId(), object);

            } else if (target instanceof TDataOutput) {
                TDataOutput targetDataOutput = (TDataOutput) target;
                Object object = data.remove(dataOutput.getId());
                setDataObject(targetDataOutput.getId(), object);

            } else
                throw new UnsupportedOperationException("Unsupported target for DataOutputAssociation " + target.getClass().getName());

        }
    }

    public DataContextAccess createDataContextAccess() {
        return new DataContextAccess() {

            @Override
            public Map<String, Object> getDataInputs() {
                return AbstractDataContext.this.getDataInputs();
            }

            @Override
            public Object getDataInput(String id) {
                return AbstractDataContext.this.getDataInput(id);
            }

            @Override
            public void setDataOutput(String id, Object value) {
                AbstractDataContext.this.setDataOutput(id, value);
            }

            @Override
            public Object getDataObject(String id) {
                return AbstractDataContext.this.getDataObject(id);
            }
        };
    }

}
