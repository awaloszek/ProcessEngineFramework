package com.bpmnengine.model;

import com.bpmnengine.MessageInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TFlowNode;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class BPMNModel {

    private static final Class[] baseObjectFactories = new Class[] {
            com.bpmnengine.gen.bpmn.ext.ObjectFactory.class,
            com.bpmnengine.gen.omg.spec.bpmn.model.ObjectFactory.class,
            com.bpmnengine.gen.omg.spec.bpmn.di.ObjectFactory.class,
            com.bpmnengine.gen.omg.spec.dd.dc.ObjectFactory.class,
            com.bpmnengine.gen.omg.spec.dd.di.ObjectFactory.class,
    };

    private static final List<Class> extObjectFactories = new LinkedList<>();

    public static void addObjectFactory(Class objectFactory) {
        extObjectFactories.add(objectFactory);
    }

    public static BPMNModel create(InputStream inputStream) {
        try {
            List<Class> objectFactories = new ArrayList<>(baseObjectFactories.length + extObjectFactories.size());
            objectFactories.addAll(Arrays.asList(baseObjectFactories));
            objectFactories.addAll(extObjectFactories);
            JAXBContext context = JAXBContext.newInstance(objectFactories.toArray(new Class[objectFactories.size()]));
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<Definitions> model = (JAXBElement<Definitions>) unmarshaller.unmarshal(inputStream);
            return new BPMNModel(model);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private final JAXBElement<Definitions> model;

    private BPMNModel(JAXBElement<Definitions> model) {
        this.model = model;
    }

    public Definitions getDefinitions() {
        return model.getValue();
    }

    public List<CatchEvent> catches(MessageInstance messageInstance) {
        return getExecutableProcess().catches(messageInstance);
    }

    public List<StartEvent> catchesStart(MessageInstance messageInstance) {
        return getExecutableProcess().catchesStart(messageInstance);
    }


    private Process getExecutableProcess() {
        return getDefinitions().getExecutableProcess();
    }

    public List<SequenceFlow> getIncoming(TFlowNode flowNode) {
        return getExecutableProcess().getSequenceFlows(flowNode.getIncoming());
    }
}
