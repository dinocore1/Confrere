package org.devsmart.confrere.packet;




public abstract class Packet {

    final byte[] mData;

    public Packet(byte[] data, int offset, int len) {
        mData = new byte[len];
        System.arraycopy(data, offset, mData, 0, len);
    }

    public final int getType() {
        int type = (mData[0] & 0xF0) >> 4;
        return type;
    }

    public final boolean getIPv6() {
        boolean isIPV6 = (mData[0] & 1) > 0;
        return isIPV6;
    }

}
