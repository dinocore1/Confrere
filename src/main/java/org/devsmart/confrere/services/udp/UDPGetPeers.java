package org.devsmart.confrere.services.udp;


import org.devsmart.confrere.Id;
import org.devsmart.confrere.Utils;

import java.net.InetSocketAddress;

public class UDPGetPeers {

    public Id id;
    public String ad;

    public InetSocketAddress getSocketAddress() {
        return Utils.parseSocketAddress(ad);
    }
}
