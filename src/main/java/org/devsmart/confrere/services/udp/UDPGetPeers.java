package org.devsmart.confrere.services.udp;


import org.devsmart.confrere.Id;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class UDPGetPeers {

    public Id id;
    public String ad;

    public SocketAddress getSocketAddress() throws UnknownHostException {
        final int i = ad.indexOf(":");
        String addstr = ad.substring(0, i);
        int port = Integer.parseInt(ad.substring(i+1, ad.length()-(i+1)));
        InetAddress address = InetAddress.getByName(ad);
        return new InetSocketAddress(address, port);
    }
}
