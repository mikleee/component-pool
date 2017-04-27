package com.virtual1.componentpool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Mikhail Tkachenko.
 */
class Utils implements Serializable {

    static int serializedSize(Serializable s) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(s);
            return byteArrayOutputStream.size();
        } catch (IOException e) {
            throw new RuntimeException("Cant evaluate size of object " + s.getClass(), e);
        }
    }

}
