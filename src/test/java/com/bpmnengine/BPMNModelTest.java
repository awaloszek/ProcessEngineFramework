/*
 * Created on 08.06.17
 *
 * Copyright (c) Zetcom AG, 2017
 *
 * $$Author$$
 * $$Revision$$
 * $$Date$$
 */
package com.bpmnengine;

import com.bpmnengine.model.BPMNModel;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author André Waloszek
 */
public class BPMNModelTest {

    @Test
    public void testCatchesMessageEvent() {
        InputStream inputStream = getClass().getResourceAsStream("../../MessageStartEvent.bpmn.xml");
        BPMNModel model = BPMNModel.create(inputStream);
        assertEquals(1, model.catchesStart(new DefaultMessageInstance("MessageId", "Message Object")).size());
    }
}
