package org.karltrout.networking.messaging;

import java.io.*;

/**
 * Base Message Class.
 * Created by karltrout on 10/20/17.
 */
public class SimulationMessage implements IMessage, Serializable {

    private Double time;

    @Override
    public byte[] asByteArray() {

        try(ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream()){

            ObjectOutput out = new ObjectOutputStream(byteArrayStream);
            out.writeObject(this);
            out.flush();
            return byteArrayStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void setMessageTime(double time) {
        this.time = time;
    }

    @Override
    public double getMessageTime() { return time; }

}
