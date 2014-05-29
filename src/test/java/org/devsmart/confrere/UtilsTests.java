package org.devsmart.confrere;


import com.google.common.net.InetAddresses;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilsTests {

    @Test
    public void testParseSocketAddress() {
        String value = "10.10.2.5:512";
        InetSocketAddress address = Utils.parseSocketAddress(value);
        assertEquals(new InetSocketAddress(InetAddresses.forString("10.10.2.5"), 512), address);

        value = "::1:9000";
        address = Utils.parseSocketAddress(value);
        assertEquals(new InetSocketAddress(InetAddresses.forString("::1"), 9000), address);

    }

}
