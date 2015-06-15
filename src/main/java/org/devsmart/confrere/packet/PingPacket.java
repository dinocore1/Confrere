package org.devsmart.confrere.packet;


import org.devsmart.confrere.Id;
import org.devsmart.confrere.PacketFactory;
import org.devsmart.confrere.Utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class PingPacket extends Packet {

    private Id mFrom;
    private InetSocketAddress mAddress;

    public PingPacket(byte[] data, int offset, int len) {
        super(data, offset, len);
    }

    public Id getFrom() {
        if(mFrom == null) {
            mFrom = new Id(mData, PacketFactory.HEADER_SIZE);
        }
        return mFrom;
    }

    public InetSocketAddress getAddress() {
        if(mAddress == null) {
            try {
                byte[] addressBytes = new byte[getIPv6() ? 16 : 4];
                System.arraycopy(mData, PacketFactory.HEADER_SIZE + Id.NUM_BYTES, addressBytes, 0, addressBytes.length);
                InetAddress address = InetAddress.getByAddress(addressBytes);

                int port = Utils.readUInt16(mData, PacketFactory.HEADER_SIZE + Id.NUM_BYTES + addressBytes.length);

                mAddress = new InetSocketAddress(address, port);
            } catch (UnknownHostException e) {}

        }
        return mAddress;
    }


}
