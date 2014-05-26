package org.devsmart.confrere.services.udp;


import com.google.inject.Inject;
import org.devsmart.confrere.Context;
import org.devsmart.confrere.Id;
import org.devsmart.confrere.services.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.List;

public class UDPMessageService implements AbstractService, UDPClient.Callback {

    protected static final Logger logger = LoggerFactory.getLogger(UDPMessageService.class);

    private UDPClient mClient;
    private UDPPeerRoutingTable mPeerRoutingTable;
    private SocketAddress mSocketAddress;
    private Context mContext;


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
    }

    @Override
    public void stop() {
        mClient.stop();
    }

    @Override
    public void receivePing(UDPPeer from) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                mPeerRoutingTable.getPeer(from);
                mClient.sendPong(mContext.localId, from.socketAddress);
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
    public void receiveGetPeersRsp(final UDPGetPeers[] resp) {
        mContext.mainThread.execute(new Runnable() {
            @Override
            public void run() {
                for(UDPGetPeers p : resp){
                    UDPPeer peer = new UDPPeer(p.id, p.getSocketAddress());
                    mPeerRoutingTable.getPeer(peer);
                }
            }
        });
    }

    @Override
    public void receiveRoute(Id target, byte[] payload) {

    }
}
