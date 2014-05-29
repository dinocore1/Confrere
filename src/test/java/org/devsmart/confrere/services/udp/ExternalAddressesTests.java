package org.devsmart.confrere.services.udp;


import com.google.common.net.InetAddresses;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

public class ExternalAddressesTests {

    @Test
    public void doTest() {
        ExternalAddresses ext = new ExternalAddresses(3);


        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.3"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.1"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.1"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.3"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.1"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.4"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.4"), 1));
        ext.addExternalAddress(new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1));

        InetSocketAddress[] bestAddresses = ext.getBestAddresses();
        assertEquals(3, bestAddresses.length);

        InetSocketAddress[] expected = new InetSocketAddress[]{
                new InetSocketAddress(InetAddresses.forString("10.10.10.2"), 1),
                new InetSocketAddress(InetAddresses.forString("10.10.10.1"), 1),
                new InetSocketAddress(InetAddresses.forString("10.10.10.3"), 1)
        };

        assertArrayEquals(expected, bestAddresses);


    }
}
