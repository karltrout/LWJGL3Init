package org.karltrout.networking.Server;

/**
 * Created by karltrout on 10/17/17.
 * Test case
 */
import io.netty.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;

public class UdpServer {

    private final static Logger logger = LogManager.getLogger();
    private final static int port = 9956;
    private final static String groupAddress = "228.5.6.7";

    public UdpServer() {
    }

    public static void main(String args[]){

        byte[] buf = new byte[256];

        MulticastSocket multicastSocket = null;
        try{
            InetAddress group = InetAddress.getByName(groupAddress);
            multicastSocket = new MulticastSocket(port);
            multicastSocket.setInterface(NetUtil.LOCALHOST);
            multicastSocket.joinGroup(group);

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        buf= "hello World".getBytes();

        try {
            InetAddress multicastGroup = InetAddress.getByName(groupAddress);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, multicastGroup, port);

            long startTime = System.nanoTime();
            logger.info("START TIME: " + startTime / 1000_000_000.0);

            int x = 0;
            while (x<1000000){
                multicastSocket.send(packet);
                x++;
            }

            long endTime = System.nanoTime();
            logger.info("END TIME: " + endTime / 1000_000_000.0);
            logger.info("Time Delta: " + (endTime - startTime)/ 1000_000_000.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
