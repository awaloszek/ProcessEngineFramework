package com.bpmnengine;

import com.bpmnengine.model.BPMNModel;
import com.bpmnengine.util.State;
import com.bpmnengine.util.Stellenausschreibung;
import org.junit.Test;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author André Waloszek
 */
public class StellenausschreibungTest {

    @Test
    public void test() {
        InputStream inputStream = getClass().getResourceAsStream("../../Stellenausschreibung.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);

        MessageInstance messageInstance = new DefaultMessageInstance("RequestMessage", "Java Developer");

        GlobalProcessInstance processInstance = new GlobalProcessInstance(UUID.randomUUID().toString(), model);

        processInstance.trigger(new DefaultMessageInstance("RequestMessage", "Java Developer"));

        List<UserTaskInstance> activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        // Stelle beschreiben
        UserTaskInstance activeUserTaskInstance = activeUserTaskInstances.get(0);
        Stellenausschreibung stellenausschreibung = (Stellenausschreibung) activeUserTaskInstance.getDataContext().getDataInput("StelleBeschreibenDataInput");
        stellenausschreibung.setState(State.CHECK);
        activeUserTaskInstance.getDataContext().setDataOutput("StelleBeschreibenDataOutput", stellenausschreibung);
        activeUserTaskInstance.completing();
        assertEquals("SBPersonal", activeUserTaskInstance.getResourceIntance().getId());

        activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        // Stellenbeschreibung pruefen
        activeUserTaskInstance = activeUserTaskInstances.get(0);
        stellenausschreibung = (Stellenausschreibung) activeUserTaskInstance.getDataContext().getDataInput("StellenbeschreibungPruefenDataInput");
        stellenausschreibung.setState(State.CHECK);
        activeUserTaskInstance.getDataContext().setDataOutput("StellenbeschreibungPruefenDataOutput", stellenausschreibung);
        activeUserTaskInstance.completing();
        assertEquals("Fuehrungskraft", activeUserTaskInstance.getResourceIntance().getId());

        activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        // Stellenbeschreibung korrigieren
        activeUserTaskInstance = activeUserTaskInstances.get(0);
        stellenausschreibung = (Stellenausschreibung) activeUserTaskInstance.getDataContext().getDataInput("StellenbeschreibungKorrigierenDataInput");
        stellenausschreibung.setState(State.CHECK);
        activeUserTaskInstance.getDataContext().setDataOutput("StellenbeschreibungKorrigierenDataOutput", stellenausschreibung);
        activeUserTaskInstance.completing();
        assertEquals("SBPersonal", activeUserTaskInstance.getResourceIntance().getId());

        activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        // Stellenbeschreibung pruefen
        activeUserTaskInstance = activeUserTaskInstances.get(0);
        stellenausschreibung = (Stellenausschreibung) activeUserTaskInstance.getDataContext().getDataInput("StellenbeschreibungPruefenDataInput");
        stellenausschreibung.setState(State.APPROVED);
        activeUserTaskInstance.getDataContext().setDataOutput("StellenbeschreibungPruefenDataOutput", stellenausschreibung);
        activeUserTaskInstance.completing();
        assertEquals("Fuehrungskraft", activeUserTaskInstance.getResourceIntance().getId());

        activeUserTaskInstances = processInstance.getActiveUserTaskInstances();
        assertEquals(1, activeUserTaskInstances.size());

        // Ausschreibung anstossen
        activeUserTaskInstance = activeUserTaskInstances.get(0);
        stellenausschreibung = (Stellenausschreibung) activeUserTaskInstance.getDataContext().getDataInput("AusschreibungAnstossenDataInput");
        stellenausschreibung.setState(State.EXECUTE);
        activeUserTaskInstance.getDataContext().setDataOutput("AusschreibungAnstossenDataOutput", stellenausschreibung);
        activeUserTaskInstance.completing();
        assertEquals("SBPersonal", activeUserTaskInstance.getResourceIntance().getId());

        assertFalse(processInstance.isRunning());
    }


}
