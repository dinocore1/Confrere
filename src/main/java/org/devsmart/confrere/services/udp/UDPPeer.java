package org.devsmart.confrere.services.udp;

import org.devsmart.confrere.Id;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPPeer {

    public static final long DYINGTIMEOUT = 60*1000;
    public static final long DEADTIMEOUT = 2*60*1000;

    public static enum State {
        UNKNOWN,
        ALIVE,
        DYING,
        DEAD
    }

    public final Id id;
    public SocketAddress socketAddress;
    protected long mLastSeenMillisec = -1;

    public UDPPeer(Id id, SocketAddress socketAddress) {
        this.id = id;
        socketAddress = socketAddress;
    }

    public void messageReceived() {
        mLastSeenMillisec = 0;
    }

    public State getState() {
        State retval = null;
        if(mLastSeenMillisec == -1){
            retval = State.UNKNOWN;
        } if(mLastSeenMillisec < DYINGTIMEOUT) {
            retval = State.ALIVE;
        } else if(mLastSeenMillisec < DEADTIMEOUT){
            retval = State.DYING;
        } else {
            retval = State.DEAD;
        }
        return retval;
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = false;
        if(obj instanceof UDPPeer){
            retval = id.equals(((UDPPeer) obj).id);
        }
        return retval;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("UDPPeer[%s/%s]", id.toHex().substring(0, 4), socketAddress);
    }

}
