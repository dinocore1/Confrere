package org.devsmart.confrere.services.udp;


import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import org.devsmart.confrere.Context;
import org.devsmart.confrere.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UDPMessageService implements UDPClient.Callback {

    protected static final Logger logger = LoggerFactory.getLogger(UDPMessageService.class);
    public static final int MAX_PEERS_BUCKET = 8;
    public static final int MAX_EXTERNAL_ADDRESSES = 10;

    private UDPClient mClient;
    protected UDPPeerRoutingTable mPeerRoutingTable;
    private SocketAddress mSocketAddress;
    private Context mContext;
    private ScheduledFuture<?> mPrunePeersTask;
    private ScheduledFuture<?> mBootstrapMaintanceTask;
    private ExternalAddresses mExternalAddresses = new ExternalAddresses(MAX_EXTERNAL_ADDRESSES);


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

    public void start() {
        mClient.start(mSocketAddress);
        mBootstrapMaintanceTask = mContext.mainThread.scheduleWithFixedDelay(mBootstrapTaskRunnable, 30, 30, TimeUnit.SECONDS);
        mPrunePeersTask = mContext.mainThread.scheduleWithFixedDelay(mPrunePeersTaskRunnable, 2, 2, TimeUnit.MINUTES);
    }

    public void stop() {
        if(mPrunePeersTask != null){
            mPrunePeersTask.cancel(false);
        }
        if(mBootstrapMaintanceTask != null){
            mBootstrapMaintanceTask.cancel(false);
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
                logger.trace("receive ping from {}", from);

                UDPPeer peer = mPeerRoutingTable.getPeer(from);
                if(isInterested(peer)) {
                    peer.scheduleMaintenance(mContext.mainThread, mContext.localId, mClient);
                }
                mClient.sendPong(mContext.localId, from.socketAddress);
            }
        });
    }

    @Override
    public void receivePong(final UDPPeer from, final InetSocketAddress externalAddress) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                logger.trace("receive pong from {} external address: {}", from, externalAddress);
                mExternalAddresses.addExternalAddress(externalAddress);
                mPeerRoutingTable.getPeer(from);
            }
        });
    }



    @Override
    public void receiveGetPeers(final Id target, final InetSocketAddress from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                logger.trace("receive ping from {}", from);

                List<UDPPeer> peers = mPeerRoutingTable.getPeers(target, 8);
                mClient.sendGetPeersResponse(peers, from);
            }
        });
    }

    @Override
    public void receiveGetPeersRsp(final UDPGetPeers[] resp, final InetSocketAddress from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                logger.trace("receive getpeersresp from {}", from);

                for(UDPGetPeers p : resp){
                    UDPPeer peer = mPeerRoutingTable.getPeer(new UDPPeer(p.id, p.getSocketAddress()));
                    if(isInterested(peer)){
                        peer.scheduleMaintenance(mContext.mainThread, mContext.localId, mClient);
                    }
                }
            }
        });
    }

    @Override
    public void receivePayload(Id target, byte[] payload, InetSocketAddress from) {

    }

    protected Runnable mBootstrapTaskRunnable = new Runnable() {

        @Override
        public void run() {
            logger.trace("Running bootstrap maintenance");

            ArrayList<UDPPeer> allPeers = new ArrayList<UDPPeer>(mPeerRoutingTable.getAllPeers());
            Collections.shuffle(allPeers);

            Iterator<UDPPeer> it = allPeers.iterator();
            int count = 0;
            while(it.hasNext() && count < 8) {
                UDPPeer peer = it.next();
                mClient.sendGetPeers(mContext.localId, peer.socketAddress);
                count++;
            }
        }
    };

    private static final Comparator<UDPPeer> NewestFirst = new Comparator<UDPPeer>() {
        @Override
        public int compare(UDPPeer udpPeer, UDPPeer udpPeer2) {
            return Longs.compare(udpPeer2.mFirstSeen, udpPeer.mFirstSeen);
        }
    };

    protected Runnable mPrunePeersTaskRunnable = new Runnable() {

        @Override
        public void run() {
            logger.trace("Running prune task");

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

    public void addPeer(final InetSocketAddress address) {
        mContext.mainThread.execute(new Runnable(){
            @Override
            public void run() {
                mClient.sendPing(mContext.localId, address);
            }
        });
    }
}
