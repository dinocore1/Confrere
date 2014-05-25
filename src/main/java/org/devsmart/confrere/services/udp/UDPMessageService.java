package org.devsmart.confrere.services.udp;


import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.devsmart.confrere.Context;
import org.devsmart.confrere.Id;
import org.devsmart.confrere.RoutingTable;
import org.devsmart.confrere.Utils;
import org.devsmart.confrere.services.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class UDPMessageService extends AbstractService {

    protected static final Logger logger = LoggerFactory.getLogger(UDPMessageService.class);
    private static final int RECEIVE_TIMEOUT = 500;




    private final SocketAddress mSocketAddress;
    private DatagramSocket mSocket;
    private boolean mIsRunning = false;
    private Future<?> mReceiveTask;
    private UDPPeerRoutingTable mPeerRoutingTable;

    @Inject
    Provider<Gson> mGsonProvider;

    public UDPMessageService(Context context, SocketAddress socketAddress) {
        super(context);
        mSocketAddress = socketAddress;
    }

    @Override
    public synchronized void start() {
        if(mIsRunning){
            logger.warn("service already started");
            return;
        }
        mReceiveTask = Utils.IOThreads.submit(new Runnable() {

            private void setup(){
                try {
                    mSocket = new DatagramSocket(mSocketAddress);
                    mSocket.setSoTimeout(RECEIVE_TIMEOUT);
                } catch (IOException e){
                    logger.error("", e);
                    mIsRunning = false;
                }
            }

            @Override
            public void run() {
                logger.info("Starting UDP on {}", mSocketAddress);
                mIsRunning = true;
                try {
                    setup();
                    while (mIsRunning) {
                        try {
                            final int bufSize = mSocket.getReceiveBufferSize();
                            byte[] buf = new byte[bufSize];
                            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            mSocket.receive(packet);
                            mContext.mainThread.execute(new Runnable(){
                                @Override
                                public void run() {
                                    receive(packet);
                                }
                            });


                        } catch (SocketTimeoutException e) {
                        } catch (IOException e) {
                            logger.error("unexpected exception", e);
                            mIsRunning = false;
                        }
                    }
                }finally {
                    mIsRunning = false;
                    logger.info("UDP service stopping on {}", mSocketAddress);
                }
            }
        });
    }

    private static final byte PING = 0;
    private static final byte PONG = 1;
    private static final byte GETPEERS = 2;
    private static final byte GETPEERS_RSP = 3;
    private static final byte ROUTE = 4;

    private void receive(DatagramPacket packet) {
        byte[] data = packet.getData();
        switch (data[0]){
            case PING: {
                Id id = new Id(data, 1);
                UDPPeer peer = new UDPPeer(id, packet.getSocketAddress());
                mPeerRoutingTable.getPeer(peer);
                sendPong(packet.getSocketAddress());
            } break;
            case PONG: {
                Id id = new Id(data, 1);
                UDPPeer peer = new UDPPeer(id, packet.getSocketAddress());
                mPeerRoutingTable.getPeer(peer);
            } break;
            case GETPEERS: {
                Id id = new Id(data, 1);
                UDPPeer peer = new UDPPeer(id, packet.getSocketAddress());
                mPeerRoutingTable.getPeer(peer);
                sendGetPeersResponse(mPeerRoutingTable.getPeers(id, 8), packet.getSocketAddress());
            } break;
            case GETPEERS_RSP:{

            } break;
        }
    }

    private void sendGetPeersResponse(final List<UDPPeer> peers, final SocketAddress socketAddress) {
        Utils.IOThreads.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Gson gson = mGsonProvider.get();

                    UDPGetPeers[] resp = new UDPGetPeers[peers.size()];
                    Iterator<UDPPeer> it = peers.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        UDPPeer p = it.next();
                        resp[i] = new UDPGetPeers();
                        resp[i].id = p.id;
                        resp[i].ad = p.socketAddress.toString();
                        i++;
                    }

                    String str = gson.toJson(resp);
                    byte[] data = str.getBytes("UTF-8");
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, socketAddress);
                    mSocket.send(packet);
                } catch(IOException e) {
                    logger.warn("could not send UDP packet", e);
                }

            }
        });
    }

    private void sendPong(final SocketAddress address){
        Utils.IOThreads.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[Id.NUM_BYTES + 1];
                    data[0] = PONG;
                    mContext.localId.write(data, 1);
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                    mSocket.send(packet);
                } catch (IOException e) {
                    logger.warn("could not send UDP packet", e);
                }
            }
        });
    }

    @Override
    public synchronized void stop() {
        if(mReceiveTask != null){
            mIsRunning = false;
            try {
                mReceiveTask.get();
            } catch (Exception e) {
                logger.error("unhandled exception", e);
            }
        }
    }
}
