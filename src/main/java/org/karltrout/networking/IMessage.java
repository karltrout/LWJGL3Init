package org.karltrout.networking;

/**
 * Created by karltrout on 10/19/17.
 */
public interface IMessage{

    public byte[] asByteArray();

    public void setMessageTime(double time);
}
