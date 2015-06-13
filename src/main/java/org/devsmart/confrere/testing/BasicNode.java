package org.devsmart.confrere.testing;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class BasicNode extends Node {

    private static final Logger logger = LoggerFactory.getLogger(BasicNode.class);

    private class BasicNetworkInterface implements NetworkInterface {

        @Override
        public void sendMessage(InetAddress to, byte[] data) {
            synchronized (sentQueue) {
                if(mSendQueueSize + data.length <= sendQueueBufferSize){
                    Packet packet = new Packet(to, address, data);
                    sentQueue.add(packet);
                } else {
                    logger.debug("Packet dropped. Send buffer full.");
                }
            }
        }

        @Override
        public Packet receiveMessage() {
            Packet retval = null;
            synchronized (receiveQueue) {
                while(receiveQueue.isEmpty()){
                    try {
                        receiveQueue.wait();
                    } catch (InterruptedException e) {}
                }

                retval = receiveQueue.remove(0);
                mReceiveQueueSize -= retval.data.length;
            }
            return retval;
        }
    }

    //32 KB
    int receiveQueueBufferSize = 32*1024;

    //32 KB
    int sendQueueBufferSize = 32*1024;

    private int mReceiveQueueSize = 0;
    private int mSendQueueSize = 0;

    float sendSpeed = 100;
    float receiveSpeed = 100;

    public BasicNode(InetAddress address) {
        super(address);
    }

    @Override
    void receiveMessage(Packet packet) {
        synchronized (receiveQueue) {
            while(mReceiveQueueSize + packet.data.length > receiveQueueBufferSize) {
                Packet p = receiveQueue.remove(0);
                mReceiveQueueSize -= p.data.length;
                logger.debug("Packet dropped. Receive buffer full.");
            }
            receiveQueue.add(packet);
            mReceiveQueueSize += packet.data.length;
        }
    }

    @Override
    NetworkInterface getNetworkInterface() {
        return null;
    }

}
