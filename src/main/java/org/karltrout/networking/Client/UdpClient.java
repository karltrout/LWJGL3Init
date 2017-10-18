package org.karltrout.networking.Client;

/**
 * Created by karltrout on 10/17/17.
 */

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

import java.net.*;
import java.nio.charset.Charset;

/**
 * Discards any incoming data.
 */
public class UdpClient {

    private static final String multicastAddress ="228.5.6.7";
    private static final int port = 9956;
    private static final Logger logger = LogManager.getLogger();
    private static final Timer timer = new Timer();

    private InetSocketAddress multicastGroup = null;

    UdpClient(InetSocketAddress multicastGroup) throws UnknownHostException, SocketException {

        timer.init();
        this.multicastGroup = multicastGroup;

    }

    public void run() throws Exception {

        NetworkInterface ni = NetworkInterface.getByInetAddress(NetUtil.LOCALHOST4);
        final NioEventLoopGroup group = new NioEventLoopGroup();

        try {

            final Bootstrap b = new Bootstrap();
            b.group(group).channelFactory(() -> new NioDatagramChannel(InternetProtocolFamily.IPv4))
            .option(ChannelOption.IP_MULTICAST_IF, ni)
            .option(ChannelOption.SO_REUSEADDR, true)
            .localAddress(NetUtil.LOCALHOST, port)
            .handler(new ChannelInitializer<NioDatagramChannel>() {
                @Override
                public void initChannel(final NioDatagramChannel ch) throws Exception {

                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new IncomingPacketHandler("Object"));

                }
            });

            // Bind and start to accept incoming connections.
            logger.info(String.format("waiting for message on port %s  address: %s",multicastGroup.getPort(),multicastGroup.getAddress()));
            NioDatagramChannel ch = (NioDatagramChannel)b.bind(multicastGroup.getPort()).sync().channel();
            ch.joinGroup(multicastGroup, ni).sync();
            ch.closeFuture().await();


        } finally {
            logger.info("Do we need to clean anything up here?");
        }
    }

    public static void main(String[] args) throws Exception {

        new UdpClient(new InetSocketAddress(multicastAddress, port)).run();
    }

    private class IncomingPacketHandler extends  SimpleChannelInboundHandler<DatagramPacket> {

        IncomingPacketHandler( Object  parserServer){}

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

            InetAddress srcAddr = datagramPacket.sender().getAddress();
            ByteBuf buf = datagramPacket.content();
            logger.info(">>"+buf.toString(Charset.defaultCharset()).toString() + "<< From: " + srcAddr + " Time: "+timer.getElapsedTime());

        }
    }
}