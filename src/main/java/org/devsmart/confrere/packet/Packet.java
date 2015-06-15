package org.devsmart.confrere.packet;




public abstract class Packet {

    private static final int RSP = 1 << 4;

    final byte[] mData;

    public Packet(byte[] data, int offset, int len) {
        mData = new byte[len];
        System.arraycopy(data, offset, mData, 0, len);
    }

    public final int getType() {
        int type = (mData[0] & 0xF0) >> 4;
        return type;
    }

    public final boolean isResponse() {
        boolean retval = (mData[0] & RSP) > 1;
        return retval;
    }

    public final boolean isIPv6() {
        boolean retval = (mData[0] & 1) > 0;
        return retval;
    }

}
