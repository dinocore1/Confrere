package org.devsmart.confrere.services.udp;


import com.google.inject.Inject;
import org.devsmart.confrere.Context;
import org.devsmart.confrere.Id;
import org.devsmart.confrere.services.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UDPMessageService implements AbstractService, UDPClient.Callback {

    protected static final Logger logger = LoggerFactory.getLogger(UDPMessageService.class);
    public static final int MAX_PEERS_BUCKET = 8;

    private UDPClient mClient;
    protected UDPPeerRoutingTable mPeerRoutingTable;
    private SocketAddress mSocketAddress;
    private Context mContext;
    private ScheduledFuture<?> mPrunePeersTask;

    public void setContext(Context context){
        mContext = context;
        mPeerRoutingTable = new UDPPeerRoutingTable(mContext.localId);
    }

    @Inject
    public void setUDPClient(UDPClient client){
        mClient = client;
        mClient.callback = this;
    }

    public void setSocketAddress(SocketAddress address) {
        mSocketAddress = address;
    }

    @Override
    public void start() {
        mClient.start(mSocketAddress);
        mPrunePeersTask = mContext.mainThread.scheduleWithFixedDelay(mPrunePeersTaskRunnable, 2, 2, TimeUnit.MINUTES);
    }

    @Override
    public void stop() {
        if(mPrunePeersTask != null){
            mPrunePeersTask.cancel(false);
        }
        mClient.stop();
    }

    public boolean isInterested(UDPPeer peer){
        boolean retval = false;
        int numAlivePeersInBucket = 0;
        for(UDPPeer p : peer.mBucket.values()){
            if(p.getState() == UDPPeer.State.ALIVE){
                numAlivePeersInBucket++;
            }
        }
        if(numAlivePeersInBucket < MAX_PEERS_BUCKET){
            retval = true;
        }

        return retval;
    }

    @Override
    public void receivePing(final UDPPeer from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                UDPPeer peer = mPeerRoutingTable.getPeer(from);
                if(isInterested(peer)) {
                    peer.scheduleMaintenance(mContext.mainThread, mContext.localId, mClient);
                    mClient.sendPong(mContext.localId, from.socketAddress);
                }
            }
        });
    }

    @Override
    public void receivePong(final UDPPeer from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                mPeerRoutingTable.getPeer(from);
            }
        });
    }

    @Override
    public void receiveGetPeers(final Id target, final SocketAddress from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                List<UDPPeer> peers = mPeerRoutingTable.getPeers(target, 8);
                mClient.sendGetPeersResponse(peers, from);
            }
        });
    }

    @Override
    public void receiveGetPeersRsp(final UDPGetPeers[] resp, final SocketAddress from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                for(UDPGetPeers p : resp){
                    try {
                        UDPPeer peer = mPeerRoutingTable.getPeer(new UDPPeer(p.id, p.getSocketAddress()));
                        if(isInterested(peer)){
                            peer.scheduleMaintenance(mContext.mainThread, mContext.localId, mClient);
                        }
                    } catch(UnknownHostException e) {
                        logger.warn("GETPEERS response from {} contain unknown host: {}", from, p.ad);
                    }
                }
            }
        });
    }

    @Override
    public void receiveRoute(Id target, byte[] payload) {

    }

    protected Runnable mPrunePeersTaskRunnable = new Runnable() {

        private final Comparator<UDPPeer> NewestFirst = new Comparator<UDPPeer>() {
            @Override
            public int compare(UDPPeer udpPeer, UDPPeer udpPeer2) {
                return new Long(udpPeer2.mFirstSeen).compareTo(new Long(udpPeer.mFirstSeen));
            }
        };

        @Override
        public void run() {
            for(HashMap<Id, UDPPeer> bucket : mPeerRoutingTable.mPeers) {
                if(bucket.size() > MAX_PEERS_BUCKET) {
                    ArrayList<UDPPeer> peers = new ArrayList<UDPPeer>(bucket.values());

                    //remove dead peers
                    Iterator<UDPPeer> it = peers.iterator();
                    while(it.hasNext()){
                        UDPPeer peer = it.next();
                        if(peer.getState() == UDPPeer.State.DEAD){
                            peer.cancelMaintaince();
                            bucket.remove(peer.id);
                            it.remove();
                        }
                    }

                    //remove any extra peers
                    Collections.sort(peers, NewestFirst);
                    it = peers.iterator();
                    while(it.hasNext() && peers.size() > MAX_PEERS_BUCKET){
                        UDPPeer peer = it.next();
                        peer.cancelMaintaince();
                        bucket.remove(peer.id);
                        it.remove();
                    }

                }
            }
        }
    };
}
