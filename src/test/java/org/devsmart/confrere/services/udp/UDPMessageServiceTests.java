package org.devsmart.confrere.services.udp;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.devsmart.confrere.Context;
import org.devsmart.confrere.DefaultModule;
import org.devsmart.confrere.Id;
import org.devsmart.confrere.Utils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UDPMessageServiceTests {

    private Injector mInjector;

    @Before
    public void inject() {
        mInjector = Guice.createInjector(new DefaultModule());
    }

    @Before
    public void setuplogging() {
        Logger logger = (Logger) LoggerFactory.getLogger("org.devsmart");
        logger.setLevel(Level.ALL);

    }

    private Id createId(String firstHex) {
        byte[] data = new byte[Id.NUM_BYTES];

        byte[] prefix = Utils.hexToBytes(firstHex);
        System.arraycopy(prefix, 0, data, 0, prefix.length);

        return new Id(data);
    }

    private void waitForNoOp(Context context) throws ExecutionException, InterruptedException {
        context.mainThread.submit(new Runnable(){
            @Override
            public void run() {}
        }).get();
    }

    @Test
    public void testPing() throws Exception {
        final Id id1 = createId("00");
        Context context1 = new Context(id1);
        UDPMessageService service1 = mInjector.getInstance(UDPMessageService.class);
        service1.setContext(context1);
        service1.setSocketAddress(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 9000));
        service1.start();

        final Id id2 = createId("80");
        Context context2 = new Context(id2);
        UDPMessageService service2 = mInjector.getInstance(UDPMessageService.class);
        service2.setContext(context2);
        service2.setSocketAddress(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 9001));
        service2.start();

        Thread.sleep(1000);


        service1.addPeer(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 9001));

        Thread.sleep(1000);

        assertEquals(1, service2.mPeerRoutingTable.mPeers.get(0).size());
        UDPPeer peer = service2.mPeerRoutingTable.mPeers.get(0).values().iterator().next();
        assertEquals(createId("00"), peer.id);

    }

    @Test
    public void testReceiveGetPeersAndPrune() throws Exception {
        Id myId = createId("FF");
        Context context = new Context(myId);

        UDPMessageService service = mInjector.getInstance(UDPMessageService.class);
        service.setContext(context);

        SocketAddress fakeSocketAddress = new InetSocketAddress(Inet4Address.getByName("10.10.10.10"), 9000);
        UDPGetPeers[] peers = new UDPGetPeers[10];
        for(int i=0;i<10;i++){
            peers[i] = new UDPGetPeers();
            String hex = Integer.toHexString(i);
            if(hex.length() == 1){
                hex = "0" + hex;
            }
            peers[i].id = createId("00" + hex);
            peers[i].ad = "10.10.10." + i + ":3000";
        }
        service.receiveGetPeersRsp(peers, fakeSocketAddress);
        Thread.sleep(10);

        peers = new UDPGetPeers[10];
        for(int i=0;i<10;i++){
            peers[i] = new UDPGetPeers();
            String hex = Integer.toHexString(i+10);
            if(hex.length() == 1){
                hex = "0" + hex;
            }
            peers[i].id = createId("00" + hex);
            peers[i].ad = "10.10.10." + (i+10) + ":3000";
        }
        service.receiveGetPeersRsp(peers, fakeSocketAddress);

        waitForNoOp(context);
        assertEquals(20, service.mPeerRoutingTable.mPeers.get(0).size());


        Future<?> pruneTask = context.mainThread.submit(service.mPrunePeersTaskRunnable);
        pruneTask.get();

        assertEquals(UDPMessageService.MAX_PEERS_BUCKET, service.mPeerRoutingTable.mPeers.get(0).size());
        for(UDPPeer p : service.mPeerRoutingTable.mPeers.get(0).values()){
            byte[] data = new byte[Id.NUM_BYTES];
            p.id.write(data, 0);
            assertTrue(data[1] < 10);
        }

    }
}
