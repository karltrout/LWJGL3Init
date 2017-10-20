package org.karltrout.networking.Server;

import org.karltrout.networking.IMessage;

import java.io.*;
import java.util.GregorianCalendar;

/**
 * Created by karltrout on 10/19/17.
 */
public class TimeMessage implements IMessage, Serializable {

    private Double time;

    public TimeMessage() {
    }

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

    public double getTime() {
       return time;
    }
}
