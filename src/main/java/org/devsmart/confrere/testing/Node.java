package org.devsmart.confrere.testing;


import java.net.InetAddress;
import java.util.ArrayList;

public abstract class Node {

    public final InetAddress address;
    public final ArrayList<Packet> sentQueue = new ArrayList<Packet>();
    public final ArrayList<Packet> receiveQueue = new ArrayList<Packet>();

    public Node(InetAddress address) {
        this.address = address;
    }

    abstract void receiveMessage(Packet packet);
    abstract NetworkInterface getNetworkInterface();




}
