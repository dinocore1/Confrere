package org.devsmart.confrere.testing;


import java.net.InetAddress;

public interface NetworkInterface {

    void sendMessage(InetAddress address, byte[] packet);
    Packet receiveMessage();
}
