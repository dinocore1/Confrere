package org.devsmart.confrere.services.udp;


import com.google.common.net.InetAddresses;
import org.devsmart.confrere.Id;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class UDPGetPeers {

    public Id id;
    public String ad;

    public InetSocketAddress getSocketAddress() throws UnknownHostException {
        final int i = ad.indexOf(":");
        String addstr = ad.substring(0, i);
        String portstr = ad.substring(i+1, ad.length());
        int port = Integer.parseInt(portstr);
        InetAddress address = InetAddresses.forString(addstr);
        return new InetSocketAddress(address, port);
    }
}
