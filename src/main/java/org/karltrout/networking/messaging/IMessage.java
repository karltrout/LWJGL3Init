package org.karltrout.networking.messaging;

/**
 * Created by karltrout on 10/19/17.
 * Default Message Implementation for all Simulation Messages.
 */
public interface IMessage{

    byte[] asByteArray();
    void setMessageTime(double time);
    double getMessageTime();
}
