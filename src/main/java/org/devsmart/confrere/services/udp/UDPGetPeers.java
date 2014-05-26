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
        String portstr = ad.substring(i+1, ad.length());
        int port = Integer.parseInt(portstr);
        InetAddress address = InetAddress.getByName(addstr);
        return new InetSocketAddress(address, port);
    }
}
