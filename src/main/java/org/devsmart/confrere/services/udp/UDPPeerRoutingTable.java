package org.devsmart.confrere.services.udp;

import org.devsmart.confrere.Id;
import org.devsmart.confrere.RoutingTable;

import java.util.*;


public class UDPPeerRoutingTable {

    private RoutingTable mRoutingTable;
    protected ArrayList<TreeMap<Id, UDPPeer>> mPeers = new ArrayList<TreeMap<Id, UDPPeer>>(Id.NUM_BYTES*8);

    public UDPPeerRoutingTable(Id myId){
        mRoutingTable = new RoutingTable(myId);
        for(int i=0;i<Id.NUM_BYTES*8;i++){
            mPeers.add(new TreeMap<Id, UDPPeer>());
        }
    }

    public UDPPeer getPeer(UDPPeer peer){
        UDPPeer retval = peer;
        final int bucketnum = mRoutingTable.getBucket(peer.id);
        TreeMap<Id, UDPPeer> bucket = mPeers.get(bucketnum);
        UDPPeer existingPeer = bucket.get(peer.id);
        if(existingPeer != null){
            retval = existingPeer;
        } else {
            bucket.put(peer.id, peer);
            peer.setBucket(bucketnum, bucket);
        }
        retval.messageReceived();
        return retval;
    }

    public List<UDPPeer> getPeers(Id targetId, int maxNum) {
        LinkedList<UDPPeer> retval = new LinkedList<UDPPeer>();

        for(int i=mRoutingTable.getBucket(targetId);i>=0;i--){
            TreeMap<Id, UDPPeer> bucket = mPeers.get(i);
            Iterator<UDPPeer> it = bucket.values().iterator();
            while(retval.size() < maxNum && it.hasNext()){
                retval.add(it.next());
            }
            if(retval.size() >= maxNum){
                break;
            }
        }

        return retval;
    }

    private static final Comparator<UDPPeer> mLastSeenDecending = new Comparator<UDPPeer>() {
        @Override
        public int compare(UDPPeer udpPeer, UDPPeer udpPeer2) {
            return new Long(udpPeer2.mLastSeenMillisec).compareTo(new Long(udpPeer.mLastSeenMillisec));
        }
    };

    public void prune(int maxPerBucket){
        for(TreeMap<Id, UDPPeer> peers : mPeers){
            if(mPeers.size() > maxPerBucket){
                ArrayList<UDPPeer> peerlist = new ArrayList<UDPPeer>(peers.values());
                Collections.sort(peerlist, mLastSeenDecending);
                Iterator<UDPPeer> it = peerlist.iterator();
                while(peers.size() > maxPerBucket && it.hasNext()){
                    peers.remove(it.next().id);
                }
            }
        }
    }
}
