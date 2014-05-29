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

}
