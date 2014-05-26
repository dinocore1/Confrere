package org.devsmart.confrere.services.udp;


import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.devsmart.confrere.Id;
import org.devsmart.confrere.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class UDPClient {

    protected static final Logger logger = LoggerFactory.getLogger(UDPClient.class);


    public interface Callback {
        void receivePing(UDPPeer from);
        void receivePong(UDPPeer from);
        void receiveGetPeers(Id target, SocketAddress from);
        void receiveGetPeersRsp(UDPGetPeers[] resp);
        void receiveRoute(Id target, byte[] payload);
    }

    private static final byte PING = 0;
    private static final byte PONG = 1;
    private static final byte GETPEERS = 2;
    private static final byte GETPEERS_RSP = 3;
    private static final byte ROUTE = 4;

    private static final int RECEIVE_TIMEOUT = 500;

    @Inject
    Provider<Gson> mGsonProvider;
    private boolean mIsRunning = false;
    private Future<?> mReceiveTask;
    private SocketAddress mSocketAddress;
    private DatagramSocket mSocket;
    public Callback callback;


    public void start(SocketAddress address) {
        if (mIsRunning) {
            logger.warn("service already started");
            return;
        }
        mSocketAddress = address;
        mReceiveTask = Utils.IOThreads.submit(new Runnable() {

            private void setup() {
                try {
                    mSocket = new DatagramSocket(mSocketAddress);
                    mSocket.setSoTimeout(RECEIVE_TIMEOUT);
                } catch (IOException e) {
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
                            receive(packet);

                        } catch (SocketTimeoutException e) {
                        } catch (IOException e) {
                            logger.error("unexpected exception", e);
                            mIsRunning = false;
                        }
                    }
                } finally {
                    mIsRunning = false;
                    logger.info("UDP service stopping on {}", mSocketAddress);
                }
            }
        });
    }

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

    private void receive(DatagramPacket packet) {
        byte[] data = packet.getData();
        switch (data[0]){
            case PING: {
                Id id = new Id(data, 1);
                UDPPeer peer = new UDPPeer(id, packet.getSocketAddress());
                if(callback != null){
                    callback.receivePing(peer);
                }
            } break;
            case PONG: {
                Id id = new Id(data, 1);
                UDPPeer peer = new UDPPeer(id, packet.getSocketAddress());
                if(callback != null){
                    callback.receivePong(peer);
                }
            } break;
            case GETPEERS: {
                Id target = new Id(data, 1);
                if(callback != null){
                    callback.receiveGetPeers(target, packet.getSocketAddress());
                }
            } break;
            case GETPEERS_RSP:{
                Gson gson = mGsonProvider.get();
                String dataStr = null;
                try {
                    dataStr = new String(data, 1, packet.getLength()-1, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    logger.error("", e);
                    throw new RuntimeException(e);
                }
                UDPGetPeers[] resp = gson.fromJson(dataStr, UDPGetPeers[].class);
                if(callback != null){
                    callback.receiveGetPeersRsp(resp);
                }
            } break;
            case ROUTE:{
                Id id = new Id(data, 1);
                byte[] payload = new byte[packet.getLength()-(Id.NUM_BYTES+1)];
                System.arraycopy(data, Id.NUM_BYTES+1, payload, 0, payload.length);
                if(callback != null){
                    callback.receiveRoute(id, payload);
                }
            } break;
        }
    }

    public void sendPing(final Id id, final SocketAddress address) {
        Utils.IOThreads.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1 + Id.NUM_BYTES];
                    data[0] = PING;
                    id.write(data, 1);
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                    mSocket.send(packet);
                } catch (IOException e) {
                    logger.warn("could not send UDP packet", e);
                }
            }
        });
    }

    public void sendPong(final Id id, final SocketAddress address){
        Utils.IOThreads.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = new byte[Id.NUM_BYTES + 1];
                    data[0] = PONG;
                    id.write(data, 1);
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                    mSocket.send(packet);
                } catch (IOException e) {
                    logger.warn("could not send UDP packet", e);
                }
            }
        });
    }

    public void sendGetPeers(final Id target, final SocketAddress address) {
        Utils.IOThreads.execute(new Runnable(){

            @Override
            public void run() {
                try {
                    byte[] data = new byte[1 + Id.NUM_BYTES];
                    data[0] = GETPEERS;
                    target.write(data, 1);
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                    mSocket.send(packet);
                } catch (IOException e) {
                    logger.warn("could not send UDP packet", e);
                }
            }
        });
    }

    public void sendGetPeersResponse(final List<UDPPeer> peers, final SocketAddress socketAddress) {
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

    public void sendRoute(final Id target, final byte[] payload, final SocketAddress address) {
        Utils.IOThreads.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    byte[] data = new byte[1 + Id.NUM_BYTES + payload.length];
                    data[0] = ROUTE;
                    target.write(data, 1);
                    System.arraycopy(payload, 0, data, 1 + Id.NUM_BYTES, payload.length);
                    DatagramPacket packet = new DatagramPacket(data, 0, data.length, address);
                    mSocket.send(packet);
                } catch (IOException e){
                    logger.warn("could not send UDP packet", e);
                }
            }
        });
    }
}
