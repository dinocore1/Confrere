package org.devsmart.confrere.services.udp;

import com.google.common.io.BaseEncoding;
import org.devsmart.confrere.Id;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    public InetSocketAddress socketAddress;
    protected long mLastSeenMillisec = -1;
    protected long mFirstSeen = -1;
    protected int mBucketId;
    protected HashMap<Id, UDPPeer> mBucket;
    private ScheduledFuture<?> mMaintanceTask;


    public UDPPeer(Id id, InetSocketAddress socketAddress) {
        this.id = id;
        this.socketAddress = socketAddress;
    }

    public void messageReceived() {
        mLastSeenMillisec = System.currentTimeMillis();
        if(mFirstSeen == -1){
            mFirstSeen = mLastSeenMillisec;
        }
    }

    public State getState() {
        final long now = System.currentTimeMillis();
        final long lastSeen = now - mLastSeenMillisec;
        State retval = null;
        if(mLastSeenMillisec == -1){
            retval = State.UNKNOWN;
        } if(lastSeen < DYINGTIMEOUT) {
            retval = State.ALIVE;
        } else if(lastSeen < DEADTIMEOUT){
            retval = State.DYING;
        } else {
            retval = State.DEAD;
        }
        return retval;
    }

    public void setBucket(int bucketnum, HashMap<Id, UDPPeer> bucket) {
        mBucketId = bucketnum;
        mBucket = bucket;
    }

    public void scheduleMaintenance(ScheduledExecutorService mainThread, final Id myid, final UDPClient client) {
        if(mMaintanceTask == null){
            mMaintanceTask = mainThread.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    client.sendPing(myid, socketAddress);
                }
            }, 20, 20, TimeUnit.SECONDS);
        }
    }

    public void cancelMaintaince(){
        if(mMaintanceTask != null){
            mMaintanceTask.cancel(false);
            mMaintanceTask = null;
        }
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
        String idstr = BaseEncoding.base64().encode(id.getBytes());
        return String.format("UDPPeer[%s/%s]", idstr.substring(0, 4), socketAddress);
    }

}
