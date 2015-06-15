package org.devsmart.confrere;


import org.junit.Test;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class PacketFactoryTests {



    @Test
    public void testCreatePing() {
        byte[] idData = new byte[Id.NUM_BYTES];
        Arrays.fill(idData, (byte) 0xff);
        Context ctx = new Context(new Id(idData));
        PacketFactory factory = new PacketFactory(ctx);

        InetSocketAddress address = Utils.parseSocketAddress("192.168.1.1:9000");

        byte[] pkt = factory.createPing(address);

        assertArrayEquals(new byte[]{0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, -88, 1, 1, 35, 40}, pkt);
    }
}
