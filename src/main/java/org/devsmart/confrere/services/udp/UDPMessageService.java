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



    public UDPMessageService(Context context, SocketAddress socketAddress) {
        super(context);
        mSocketAddress = socketAddress;
    }

    @Override
    public synchronized void start() {

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
