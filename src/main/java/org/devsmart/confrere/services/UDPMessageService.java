package org.devsmart.confrere.services;


import org.devsmart.confrere.Context;
import org.devsmart.confrere.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UDPMessageService extends AbstractService {

    protected static final Logger logger = LoggerFactory.getLogger(UDPMessageService.class);

    private static final int RECEIVE_TIMEOUT = 500;
    private final SocketAddress mSocketAddress;
    private DatagramSocket mSocket;
    private boolean mIsRunning = false;
    private Future<?> mReceiveTask;

    public UDPMessageService(Context context, SocketAddress socketAddress) {
        super(context);
        mSocketAddress = socketAddress;
    }

    @Override
    public synchronized void start() {
        mReceiveTask = Utils.IOThreads.submit(new Runnable() {

            private void setup(){
                try {
                    mSocket = new DatagramSocket(mSocketAddress);
                    mSocket.setSoTimeout(RECEIVE_TIMEOUT);
                } catch (IOException e){

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
                            DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            mSocket.receive(packet);

                            receive(packet);

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
    private static final byte ROUTE = 4;

    private void receive(DatagramPacket packet) {
        byte[] data = packet.getData();
        switch (data[0]){
            case PING:
                break;
            case PONG:
                break;
        }
    }

    @Override
    public synchronized void stop() {
        if(mReceiveTask != null){
            mIsRunning = false;
            mReceiveTask.cancel(false);
            try {
                mReceiveTask.get();
            } catch (Exception e) {
                logger.error("unhandled exception", e);
            }
        }
    }
}
