package org.devsmart.confrere;


import org.devsmart.confrere.packet.Packet;
import org.devsmart.confrere.packet.PingPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class PacketFactory {

    public static final int HEADER_SIZE = 1;
    private static final int FLAG_IPV6 = 0;
    private static final int TYPE_PING = 0;


    private final Context mContext;

    public PacketFactory(Context context){
        mContext = context;
    }

    public static Packet parsePacket(byte[] data, int offset, int len) {
        final int type = (data[offset] & 0xF0) >> 4;

        Packet retval = null;

        switch (type) {
            case TYPE_PING:
                retval = new PingPacket(data, offset, len);
                break;

        }

        return retval;
    }

    public byte[] createPing(InetSocketAddress address) {

        byte[] byteaddress = address.getAddress().getAddress();
        final int dataSize = byteaddress.length + 2;

        byte[] retval = new byte[HEADER_SIZE+Id.NUM_BYTES+dataSize];

        final boolean isIPv6 = byteaddress.length > 4;

        //write header
        retval[0] = (byte)((TYPE_PING << 4) & (isIPv6 ? (1 << FLAG_IPV6) : 0));

        //write ID
        mContext.localId.write(retval, HEADER_SIZE);

        //write address
        System.arraycopy(byteaddress, 0, retval, HEADER_SIZE + Id.NUM_BYTES, byteaddress.length);

        //write port
        final int port = address.getPort();
        Utils.writeUInt16(retval, HEADER_SIZE + Id.NUM_BYTES + byteaddress.length, port);

        return retval;

    }


}
