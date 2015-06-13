package org.devsmart.confrere.testing;


import java.net.InetAddress;

public class Packet {

    public final InetAddress to;
    public final InetAddress from;
    public final byte[] data;

    public Packet(InetAddress to, InetAddress from, byte[] data) {
        this.to = to;
        this.from = from;
        this.data = data;
    }
}
