package com.bpmnengine.model;

import com.bpmnengine.CatchEventTrigger;
import com.bpmnengine.MessageInstance;
import com.bpmnengine.ProcessInstance;
import com.bpmnengine.gen.omg.spec.bpmn.model.TIntermediateCatchEvent;

import java.util.List;

/**
 * @author André Waloszek
 */
public class IntermediateCatchEvent extends TIntermediateCatchEvent {

    @Override
    public boolean correlates(ProcessInstance processInstance, MessageInstance messageInstance) {
        List<CatchEventTrigger> catchEventTriggers = processInstance.getCatchEventTriggers(this);
        if (catchEventTriggers.isEmpty())
            return false;

        return super.correlates(processInstance, messageInstance);
    }
}
