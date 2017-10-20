package org.karltrout.networking;

import io.netty.util.NetUtil;

import java.io.UncheckedIOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by karltrout on 10/20/17.
 */
public  class NetworkUtilities {
    public static Optional< NetworkInterface > getLocalMultiCastInterface() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(networkInterface -> {
                        try {
                            return networkInterface.supportsMulticast();
                        } catch (SocketException se) {
                            throw new UncheckedIOException(se);
                        }
                    })
                    .filter(networkInterface -> {
                        try {
                            return networkInterface.isUp();
                        } catch (SocketException se) {
                            throw new UncheckedIOException(se);
                        }
                    })
                    .filter(networkInterface -> {
                        try{
                            return !networkInterface.isLoopback();
                        } catch (SocketException se) {
                            throw new UncheckedIOException((se));
                        }
                    })
                    .findFirst();

        } catch (SocketException e) {
            return Optional.of(NetUtil.LOOPBACK_IF);
        }
    }
}
