package org.karltrout.networking.Client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.karltrout.graphicsEngine.Timer;
import org.karltrout.networking.Server.TimeMessage;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.*;
import java.nio.charset.Charset;

/**
 * Created by karltrout on 10/17/17.
 * Used for testing.
 * Generic UDP Client For testing
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
            NetworkInterface ni = NetworkInterface.getByInetAddress(NetUtil.LOCALHOST4);
            final Bootstrap b = new Bootstrap();
            b.group(group).channelFactory(() -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
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

        } catch (InterruptedException | SocketException e) {
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
                Object recievedObj = input.readObject();
                if (recievedObj instanceof TimeMessage) {
                    TimeMessage msg = (TimeMessage) recievedObj;
                    logger.info( "TimeMessage >>" + msg.getTime() + "<< From: " +
                                    srcAddr + " Time: " + timer.getElapsedTime());
                } else {
                    logger.info(">>" + buf.toString(Charset.defaultCharset()) +
                            "<< From: " + srcAddr + " Time: " + timer.getElapsedTime());
                }
            } catch (OptionalDataException e){
               e.printStackTrace();
            }
        }
    }
}