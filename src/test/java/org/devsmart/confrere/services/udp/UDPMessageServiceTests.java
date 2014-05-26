package org.devsmart.confrere.services.udp;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.devsmart.confrere.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class UDPMessageServiceTests {

    private Injector mInjector;

    @Before
    public void inject() {
        mInjector = Guice.createInjector(new DefaultModule());
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
            public void run() {

            }
        }).get();
    }

    @Test
    public void testMaintenance() throws Exception {
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
