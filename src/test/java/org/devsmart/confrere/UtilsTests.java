package org.devsmart.confrere;


import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilsTests {

    @Test
    public void testBytesToHex() {
        byte[] data = new byte[]{-1, 0, 0x0a};

        String result = Utils.bytesToHex(data);
        assertEquals("FF000A", result);
    }

    @Test
    public void testHexStringToBytes() {
        String hexString = "FF000A";
        byte[] data = Utils.hexToBytes(hexString);
        assertTrue(Arrays.equals(new byte[]{-1, 0, 0x0a}, data));
    }
}
