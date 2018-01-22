/*
 * Created on 10.06.17
 *
 * Copyright (c) Zetcom AG, 2017
 *
 * $$Author$$
 * $$Revision$$
 * $$Date$$
 */
package com.bpmnengine;

import com.bpmnengine.data.DataContextAccess;
import com.bpmnengine.model.BPMNModel;
import com.bpmnengine.model.CatchEvent;
import com.bpmnengine.model.EndEvent;
import com.bpmnengine.model.StartEvent;
import com.bpmnengine.token.Token;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author André Waloszek
 */
public class GlobalProcessInstanceTest {

    @Test
    public void testStartEventEmpty() {
        InputStream inputStream = getClass().getResourceAsStream("../../SubProcess.bpmn.xml");
        BPMNModel bpmnModel = BPMNModel.create(inputStream);
        GlobalProcessInstance globalProcessInstance = new GlobalProcessInstance("Id", bpmnModel);
        assertEquals(LifecycleState.INACTIVE, globalProcessInstance.getState());
        globalProcessInstance.start();
        assertEquals(LifecycleState.COMPLETED, globalProcessInstance.getState());
    }

    @Test
    public void testProcessDataInput() {
        InputStream inputStream = getClass().getResourceAsStream("../../ProcessDataInput.bpmn.xml");
        BPMNModel bpmnModel = BPMNModel.create(inputStream);
        GlobalProcessInstance globalProcessInstance = new GlobalProcessInstance("Id", bpmnModel);
        MessageInstance messageInstance = new DefaultMessageInstance("RequestMessage", "PayLoad");
        globalProcessInstance.trigger(messageInstance);
        assertEquals(globalProcessInstance.getDataContext().getDataObject("ProcessDataInput"), "PayLoad");
    }

    @Test
    public void testResourceParameterBinding() {
        MessagePayload messagePayload = new MessagePayload();
        InputStream inputStream = getClass().getResourceAsStream("../../ResourceParameterBinding.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        List<UserTaskInstance> userTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, userTaskInstances.size());

        UserTaskInstance userTaskInstance = userTaskInstances.get(0);
        ResourceInstance resourceIntance = userTaskInstance.getResourceIntance();
        assertEquals("TestUser", resourceIntance.get("name"));
        assertEquals("TestGroup", resourceIntance.get("group"));
    }

    @Test
    public void testTaskDataInput() {
        String dataInput = "TestValue";

        InputStream inputStream = getClass().getResourceAsStream("../../TaskDataInputOutput.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", dataInput);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        List<UserTaskInstance> userTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, userTaskInstances.size());

        UserTaskInstance userTaskInstance = userTaskInstances.get(0);
        Object userTaskMessageItemDataInput = userTaskInstance.getDataContext().getDataInput("UserTaskMessageItemDataInput");
        assertEquals("TestValue", userTaskMessageItemDataInput);
    }

    @Test
    public void testTaskDataOutput() {
        String dataInput = "TestValue";

        InputStream inputStream = getClass().getResourceAsStream("../../TaskDataInputOutput.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", dataInput);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        List<UserTaskInstance> userTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, userTaskInstances.size());

        UserTaskInstance userTaskInstance = userTaskInstances.get(0);
        userTaskInstance.getDataContext().setDataOutput("UserTaskMessageItemDataOutput", "DataOutputValue");
        userTaskInstance.completing();

        assertEquals("DataOutputValue", processInstance.getDataContext().getDataObject("ItemDataObject"));
    }

    @Test
    public void testTaskDataObjectReadOnly() {
        MessagePayload messagePayload = new MessagePayload();
        messagePayload.setUser("ReadOnly");

        InputStream inputStream = getClass().getResourceAsStream("../../DataObjectReadOnly.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);


        MessagePayload dataObject = (MessagePayload) processInstance.getDataContext().getDataObject("ItemDataObject");
        assertEquals("ReadOnly", dataObject.getUser());
    }

    @Test
    public void testMessageStartWithUserTask() {
        InputStream inputStream = getClass().getResourceAsStream("../../MessageStartWithUserTask.bpmn.xml");
        // load the BPMN model from any inputStream
        BPMNModel model = BPMNModel.create(inputStream);

        // define a payload for the message to send to the process instance
        MessagePayload messagePayload = new MessagePayload();
        // define the MessageInstance with an identifier and payload
        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);

        // instantiate the GlobalProcessInstance in LifecycleState.INACTIVE
        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);

        // trigger the StartEvent with a MessageDefinition
        processInstance.trigger(messageInstance);

        // test if there is exactly one userTaskInstance waiting for interaction
        assertEquals(1, processInstance.getActiveUserTaskInstances().size());

        // simulate the user interaction
        UserTaskInstance userTaskInstance = processInstance.getActiveUserTaskInstances().get(0);
        DataContextAccess dataContextAccess = userTaskInstance.getDataContext();
        MessagePayload object = (MessagePayload) dataContextAccess.getDataInput("UserTaskMessageItemDataInput");
        // change the value of the data input
        object.setState("done");
        // write the object to the data output
        userTaskInstance.getDataContext().setDataOutput("UserTaskMessageItemDataOutput", object);
        // finalize the user interaction with a call to completing()
        userTaskInstance.completing();

        // test if the process ended
        assertFalse(processInstance.isRunning());


        MessagePayload result = (MessagePayload) processInstance.getDataContext().getDataObject("ItemDataObject");
        // test if the dataobject was successfully changed by the user task
        assertEquals("done", result.getState());

        // test for the expected number of tokens and for expected flow
        assertEquals(1, processInstance.getTokens().size());
        assertEquals("StartEventWithMessageDefinition.SequenceFlowToUserTask.UserTask.SequenceFlowToEnd.EndEvent", processInstance.getTokens().get(0).getPathAsString());
    }

    @Test
    public void testStartEventMessageDefinition() {
        InputStream inputStream = getClass().getResourceAsStream("../../MessageStartEventScript.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", "Message payload");
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);
    }

    @Test
    public void testIntermediateCatchEvent() {
        InputStream inputStream = getClass().getResourceAsStream("../../IntermediateCatchEvent.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.start();

        assertTrue(processInstance.isRunning());

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", "payload");
        List<CatchEvent> nodes = model.catches(messageInstance);
        assertEquals(1, nodes.size());
        processInstance.trigger(messageInstance);

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testDataObject() {
        InputStream inputStream = getClass().getResourceAsStream("../../DataObject.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", "Message payload");
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

    }

    @Test
    public void testGatewayIsEither() {
        InputStream inputStream = getClass().getResourceAsStream("../../Gateway.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", new MessagePayload());
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        assertEquals(1, processInstance.getTokens().size());
        assertEquals("StartEvent_1.SequenceFlow_0xm1rn3.ExclusiveGateway_066racb.SequenceFlow_0xwxppy.scriptTask.SequenceFlow_188z8zo.EndEvent_1s7xfmt",
                processInstance.getTokens().get(0).getPathAsString());

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testGatewayIsOr() {
        InputStream inputStream = getClass().getResourceAsStream("../../Gateway.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessagePayload messagePayload = new MessagePayload();
        messagePayload.setEither(false);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        assertEquals("StartEvent_1.SequenceFlow_0xm1rn3.ExclusiveGateway_066racb.SequenceFlow_0bvucq6.Task_0381u7b.SequenceFlow_19x6s1n.EndEvent_1s7xfmt",
                processInstance.getTokens().get(0).getPathAsString());

    }

    @Test
    public void testMessageCorrelation() {
        MessagePayload messagePayload = new MessagePayload();

        InputStream inputStream = getClass().getResourceAsStream("../../MessageCorrelation.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        List<CatchEvent> nodes = model.catches(messageInstance);
        assertEquals(2, nodes.size());
        processInstance.trigger(messageInstance);

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testMessageCorrelationFails() {
        MessagePayload messagePayload = new MessagePayload();

        InputStream inputStream = getClass().getResourceAsStream("../../MessageCorrelation.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        MessagePayload anotherMessagePayload = new MessagePayload();
        anotherMessagePayload.setKey("KeyNotCorrelating");

        MessageInstance anotherMessageInstance = new DefaultMessageInstance("MessageId", anotherMessagePayload);
        List<CatchEvent> nodes = model.catches(anotherMessageInstance);
        assertEquals(2, nodes.size());
        processInstance.trigger(anotherMessageInstance);

        assertTrue(processInstance.isRunning());

    }

    @Test
    public void testUserTask() {
        MessagePayload messagePayload = new MessagePayload();

        InputStream inputStream = getClass().getResourceAsStream("../../UserTask.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", messagePayload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        List<UserTaskInstance> userTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, userTaskInstances.size());

        UserTaskInstance userTaskInstance = userTaskInstances.get(0);
        DataContextAccess dataContextAccess = userTaskInstance.getDataContext();
        MessagePayload object = (MessagePayload) dataContextAccess.getDataInput("UserTaskMessageItemDataInput");
        object.setState("done");
        userTaskInstance.getDataContext().setDataOutput("UserTaskMessageItemDataOutput", object);
        userTaskInstance.completing();
        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testParallelGatewayDiverge() {
        InputStream inputStream = getClass().getResourceAsStream("../../ParallelGatewayDiverge.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", new MessagePayload());
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testParallelGatewayConverge() {
        MessagePayload payload = new MessagePayload();

        InputStream inputStream = getClass().getResourceAsStream("../../ParallelGatewayConverge.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", payload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        MessagePayload result = (MessagePayload) processInstance.getDataContext().getDataObject("ItemDataObject");
        assertEquals(1, result.getIntValue());

        assertFalse(processInstance.isRunning());

    }

    @Test
    public void testCompensation() {
        MessagePayload payload = new MessagePayload();

        InputStream inputStream = getClass().getResourceAsStream("../../Compensation.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("MessageId", payload);
        List<StartEvent> events = model.catchesStart(messageInstance);
        assertEquals(1, events.size());

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.trigger(messageInstance);

        assertFalse(processInstance.isRunning());

    }

    @Test
    public void testSubProcess() {
        InputStream inputStream = getClass().getResourceAsStream("../../SubProcess.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.start();

        assertFalse(processInstance.isRunning());

        assertEquals("StartEvent_15779bf.SequenceFlow_0x5l3m5.Task_1iu5vz6.SequenceFlow_03u5fu3.GlobalProcessTask.SequenceFlow_0kzbmmm.EndEvent_0w0sgp5",
                processInstance.getTokens().get(0).getPathAsString());
    }

    @Test
    public void testSubProcesFlow() {
        InputStream inputStream = getClass().getResourceAsStream("../../SubProcess.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.start();

        assertFalse(processInstance.isRunning());

        SubProcessInstance subProcessInstance = null;
        for (ActivityInstance activityInstance : processInstance.getActivityInstances()) {
            if (activityInstance instanceof SubProcessInstance) {
                subProcessInstance = (SubProcessInstance) activityInstance;
            }
        }
        assertNotNull(subProcessInstance);

        Token subProcessTokenInEndEvent = null;
        for (Token token : subProcessInstance.getTokens()) {
            if (token.getPosition().getId().equals("EndEvent_124196t"))
                subProcessTokenInEndEvent = token;
        }
        assertNotNull(subProcessTokenInEndEvent);

        assertEquals("ExclusiveGateway_0iiz0gl.SequenceFlow_1mvwznp.EndEvent_124196t",
                subProcessTokenInEndEvent.getPathAsString());

        assertEquals(2, subProcessTokenInEndEvent.getParents().size());
    }

    @Test
    public void testSubProcessCompensation() {
        InputStream inputStream = getClass().getResourceAsStream("../../SubProcessCompensation.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.start();

        assertEquals(2, processInstance.getActivityInstances().size());

        ActivityInstance activityInstance1 = processInstance.getActivityInstances().get(0);
        ActivityInstance activityInstance2 = processInstance.getActivityInstances().get(1);

        SubProcessInstance subProcessInstance = (SubProcessInstance) (activityInstance1 instanceof SubProcessInstance
                ? activityInstance1 : activityInstance2);
        ActivityInstance activityInstance = subProcessInstance == activityInstance1
                ? activityInstance2 : activityInstance1;

        assertEquals(LifecycleState.COMPENSATED, subProcessInstance.getState());
        assertEquals(LifecycleState.COMPLETED, activityInstance.getState());

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testSubProcessDataObjectScope() {
        InputStream inputStream = getClass().getResourceAsStream("../../SubProcessDataObjectScope.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("Id", model);
        processInstance.start();

        List<UserTaskInstance> activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        assertEquals("TopLevelDataObjectValue", activeUserTaskInstances.get(0).getDataContext().getDataObject("DataObject_0owmgic"));
        assertEquals(null, activeUserTaskInstances.get(0).getDataContext().getDataObject("DataObject_1a9n9gv"));

        assertTrue(processInstance.isRunning());
    }

    @Test
    public void testStartQuantity() {
        InputStream inputStream = getClass().getResourceAsStream("../../StartQuantity.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("StartQuantity", model);
        processInstance.start();

        assertEquals(1, processInstance.getActivityInstances().size());
        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testUncontrolledFlow() {
        InputStream inputStream = getClass().getResourceAsStream("../../UncontrolledFlow.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("UncontrolledFlow", model);
        processInstance.start();

        List<ActivityInstance> uncontrolled = new ArrayList<>(2);
        for (ActivityInstance activityInstance : processInstance.getActivityInstances()) {
            if (activityInstance.getActivity().getId().equals("Task_1w2kgwb"))
                uncontrolled.add(activityInstance);
        }
        assertEquals(2, uncontrolled.size());
        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testMultipleOutgoingFlows() {
        InputStream inputStream = getClass().getResourceAsStream("../../MultipleOutgoingFlows.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("MultipleOutgoingFlows", model);
        processInstance.start();

        assertFalse(processInstance.isRunning());

        assertEquals(3, processInstance.getTokens().size());

        List<Token> tokenInEndEvents = new ArrayList<>(2);
        for (EndEvent endEvent : model.getDefinitions().getExecutableProcess().getEndEvents()) {
            for (Token token : processInstance.getTokens()) {
                if (token.getPosition().equals(endEvent)) {
                    tokenInEndEvents.add(token);
                }
            }
        }

        assertEquals(2, tokenInEndEvents.size());
        assertFalse(tokenInEndEvents.get(0).getPosition().equals(tokenInEndEvents.get(1).getPosition()));
    }

    @Test
    public void testMultipleOutgoingFlowsWithCondition() {
        InputStream inputStream = getClass().getResourceAsStream("../../MultipleOutgoingFlowsWithCondition.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("MultipleOutgoingFlowsWithCondition", model);
        processInstance.start();

        assertFalse(processInstance.isRunning());
        assertEquals(1, processInstance.getTokens().size());
        assertEquals("StartEvent_1.SequenceFlow_0p49yxs.Task_1tr62mp.SequenceFlow_02gqbxi.EndEvent_1nxw21c",
                processInstance.getTokens().get(0).getPathAsString());
    }

    @Test
    public void testExclusiveGatewayDefaultFlow() {
        InputStream inputStream = getClass().getResourceAsStream("../../ExclusiveGatewayDefaultFlow.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("ExclusiveGatewayDefaultFlow", model);
        processInstance.start();

        assertFalse(processInstance.isRunning());
        assertEquals(1, processInstance.getTokens().size());
        assertEquals("DefaultEndEvent", processInstance.getTokens().get(0).getPosition().getId());
    }

    @Test
    public void testActivityDefaultFlow() {
        InputStream inputStream = getClass().getResourceAsStream("../../ActivityDefaultFlow.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        GlobalProcessInstance processInstance = new GlobalProcessInstance("ActivityDefaultFlow", model);
        processInstance.start();

        for (Token token : processInstance.getTokens()) {
            if (token.getPosition() instanceof EndEvent)
                assertEquals("DefaultEndEvent", token.getPosition().getId());
        }

        assertFalse(processInstance.isRunning());
    }

    @Test
    public void testWaitForValidInputSet() {
        assertTrue(false);
    }

    @Test
    public void testSubProcessUserTask() {
        assertTrue(false);
    }

    @Test
    public void testSubProcessIntermediateCatchEvent() {
        assertTrue(false);
    }

    @Test
    public void testSubSubProcess() {
        assertTrue(false);
    }

}
