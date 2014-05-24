package org.devsmart.confrere;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

    public static final ExecutorService IOThreads = Executors.newCachedThreadPool();

    private final static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public static String bytesToHex(byte[] data, int offset, int len){
        char[] hexChars = new char[len * 2];
        int v;
        for ( int j = 0; j < len; j++ ) {
            v = data[offset + j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte[] data) {
        return bytesToHex(data, 0, data.length);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
