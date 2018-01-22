package com.bpmnengine.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author André Waloszek
 */
public class Data {

    private static byte[] write(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {

            out.writeObject(object);
            out.flush();

            return bos.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("Unable to write object to byte array", ex);
        }
    }

    private static Object read(byte[] object) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(object);
             ObjectInputStream in = new ObjectInputStream(bis)) {

            return in.readObject();

        } catch (Exception ex) {
            throw new RuntimeException("Unable to read object from byte array", ex);
        }
    }

    public static Object copy(Object source) {
        byte[] content = write(source);
        return read(content);
    }
}
