package org.karltrout.networking.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.karltrout.graphicsEngine.Timer;
import org.karltrout.networking.NetworkUtilities;
import org.karltrout.networking.messaging.TakeoffMessage;
import org.karltrout.networking.messaging.TimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.*;
import java.nio.charset.Charset;

/**
 * Created by karltrout on 10/17/17.
 * Used for testing.
 * Generic UDP client For testing
 *
 * Note: JVM Options:
 * -Dlog4j.configurationFile=log4j2.xml -Djava.net.preferIPv4Stack=true
 */
public class UdpClient implements Runnable {

    private static final String multicastAddress ="228.5.6.7";
    private static final int port = 9956;
    private static final Logger logger = LogManager.getLogger();
    private static final Timer timer = new Timer();

    private InetSocketAddress multicastGroup = null;

    /**
     * @param multicastGroup Socket inet4 Address for the UDP message group.
     */
    private UdpClient(InetSocketAddress multicastGroup) {
        timer.init();
        this.multicastGroup = multicastGroup;
    }

    @Override
    public void run(){

        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            NetworkInterface ni = NetworkUtilities.getLocalMultiCastInterface().get();
            logger.info("Network Interface Name: " + ni.getName());
            final Bootstrap b = new Bootstrap();
            b.group(group).channelFactory(NioDatagramChannel::new)
            .option(ChannelOption.IP_MULTICAST_IF, ni)
            .option(ChannelOption.SO_REUSEADDR, true)
            .localAddress(NetUtil.LOCALHOST, port)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                public void initChannel(final NioDatagramChannel ch) throws Exception {

                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new IncomingPacketHandler());

                }
            });

            // Bind and start to accept incoming connections.
            logger.info(
                    String.format(
                            "waiting for message on port %s  address: %s",
                            multicastGroup.getPort(),multicastGroup.getAddress()));

            NioDatagramChannel ch = (NioDatagramChannel)b.bind(multicastGroup.getPort()).sync().channel();
            ch.joinGroup(multicastGroup, ni).sync();
            ch.closeFuture().await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            logger.info("Do we need to clean anything up here?");
        }
    }

    public static void main(String[] args) throws Exception {
        new UdpClient(new InetSocketAddress(multicastAddress, port)).run();
    }

    private class IncomingPacketHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        IncomingPacketHandler(){}

        @Override
        protected void channelRead0(
                ChannelHandlerContext channelHandlerContext,
                DatagramPacket datagramPacket) throws Exception {

            InetAddress srcAddr = datagramPacket.sender().getAddress();
            ByteBuf buf = datagramPacket.content();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            ByteArrayInputStream byteArrayStream = new ByteArrayInputStream(bytes);

            try(ObjectInput input = new ObjectInputStream(byteArrayStream)) {
                Object receivedObj = input.readObject();
                if (receivedObj instanceof TimeMessage) {
                    TimeMessage msg = (TimeMessage) receivedObj;
                    logger.info( "TimeMessage >>" + msg.getMessageTime() + "<< From: " +
                                    srcAddr + " Time: " + timer.getElapsedTime());
                } else if (receivedObj instanceof TakeoffMessage){
                    TakeoffMessage msg = (TakeoffMessage) receivedObj;
                    logger.info( "TakeoffMessage >>" + msg.getMessageTime() + "<< Speed " + msg.getSpeed()+ " From: " +
                            srcAddr + " Time: " + timer.getElapsedTime());
                }
                else {
                    logger.info(">>" + buf.toString(Charset.defaultCharset()) +
                            "<< From: " + srcAddr + " Time: " + timer.getElapsedTime());
                }
            } catch (OptionalDataException e){
               e.printStackTrace();
            }
        }
    }
}