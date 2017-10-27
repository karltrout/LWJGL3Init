package org.karltrout.networking.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.karltrout.graphicsEngine.Timer;
import org.karltrout.networking.NetworkUtilities;
import org.karltrout.networking.messaging.IMessage;
import org.karltrout.networking.messaging.TakeoffMessage;
import org.karltrout.networking.messaging.TimeMessage;
import org.karltrout.physics.Dynamics;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by karltrout on 10/17/17.
 * Test case.
 * Note: JVM Options:
 * -Dlog4j.configurationFile=log4j2.xml -Djava.net.preferIPv4Stack=true
 */
public class UdpServer implements Runnable {

    private final static Logger logger = LogManager.getLogger();
    private final static int port = 9956;
    private final static String groupAddress = "228.5.6.7";
    private boolean running;
    private ConcurrentLinkedQueue<IMessage> linkedQueue;
    private int counter;
    private Timer timer;
    private double startTime;
    private double staticsRate;

    private UdpServer() {}

    public static void main(String args[]){

       Timer timer = new Timer();
       timer.init();

       UdpServer server = new UdpServer();

       ExecutorService executor = Executors.newSingleThreadExecutor();
       executor.submit(server);

        double time, drag;
        double velocity = 0.0d;
        float distance = 0f;
        TakeoffMessage.Builder builder;
        while (velocity < 77.17){ //150 Knots

            try { // take a break for a sec.
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            builder = new TakeoffMessage.Builder();
            time = timer.getElapsedTime();
            drag = Dynamics.aeronauticalDrag(10.13f, .018130f, 909.22f, velocity);
            velocity = Dynamics.currentVelocity(velocity, (4*332300.0d * .7 ) - drag, 596000, time);  //596000 kg full weight

            distance += velocity * time;
            TakeoffMessage toMessage = builder.withSpeed(velocity).build();
            server.queue(toMessage);

            logger.info("Time :"+timer.getTimeFromStart()+" t: "+ time+ " v: "+velocity+" d: "+(velocity * time));

        }
       logger.info(String.format("Sent %d packets, with a rate of %f",
               server.getCounter(), server.getStaticsRate()));
       server.quit();

       System.exit(1);
    }

    @Override
    public void run() {
        this.linkedQueue = new ConcurrentLinkedQueue<>();

        this.timer = new Timer();
        timer.init();
        this.running = true;
        MulticastSocket multicastSocket = null;

        try{

            InetAddress group = InetAddress.getByName(groupAddress);
            multicastSocket = new MulticastSocket(port);
            NetworkInterface networkInterface = NetworkUtilities.getLocalMultiCastInterface().get();
            multicastSocket.setNetworkInterface(networkInterface);
            multicastSocket.joinGroup(group);

            logger.info("Network Interface id is : " + networkInterface.getName());

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {

            InetAddress multicastGroup = InetAddress.getByName(groupAddress);

            startTime = timer.getTime();
            logger.info("START TIME: " + startTime );

            byte[] buf = new byte[1024];
            float time = 0;
            IMessage message;

            while(running){

                time += timer.getElapsedTime();

                if(time > 1){
                  message = new TimeMessage();
                  calculateStatus();
                  time = 0;
                }
                else {
                    message = linkedQueue.poll();
                }

                if(message != null && multicastSocket != null) {
                    message.setMessageTime(timer.getTime());
                    buf = message.asByteArray();
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, multicastGroup, port);
                    multicastSocket.send(packet);
                }
                else{
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.info("server Messaging Idle, Thread interrupted.");
                    }
                }

            }

            double endTime = timer.getTime();
            logger.info("END TIME: " + endTime );
            logger.info("Time Delta: " + (endTime - startTime));

            if(multicastSocket != null && !multicastSocket.isClosed()) {
                multicastSocket.close();
                logger.info("Multicast server is closed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quit(){
        this.running = false;
    }

    public void queue(IMessage message){

        linkedQueue.add(message);
        calculateStatus();

    }

    private void calculateStatus(){
        this.counter++;
        this.staticsRate = (timer.getTime() - startTime) / counter;
    }

    private double getStaticsRate() {
        return staticsRate;
    }
    private int getCounter() {
        return counter;
    }
}
