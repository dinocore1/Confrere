package org.devsmart.confrere;


import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

    public static final ExecutorService IOThreads = Executors.newCachedThreadPool();

    public static InetSocketAddress parseSocketAddress(String value) {
        final int i = value.lastIndexOf(":");
        String addstr = value.substring(0, i);
        String portstr = value.substring(i+1, value.length());
        int port = Integer.parseInt(portstr);
        InetAddress address = InetAddresses.forString(addstr);
        return new InetSocketAddress(address, port);
    }

    public static void writeUInt16(byte[] buff, int offset, int value) {
        buff[offset] = (byte) ((value & 0xFF00) >> 8);
        buff[offset+1] = (byte) (value & 0x00FF);
    }

    public static int readUInt16(byte[] buff, int offset) {
        int b1 = 0xFF & buff[offset];
        int b2 = 0xFF & buff[offset+1];

        return (b1 << 8 | b2) & 0xFFFF;
    }
}
