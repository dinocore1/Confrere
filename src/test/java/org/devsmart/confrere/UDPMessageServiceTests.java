package org.devsmart.confrere;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.devsmart.confrere.services.udp.UDPMessageService;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UDPMessageServiceTests {

    private Injector mInjector;

    @Before
    public void inject() {
        mInjector = Guice.createInjector(new DefaultModule());
    }

    @Test
    public void testStartup() throws UnknownHostException, InterruptedException {
        Id myId = mInjector.getInstance(IdFactory.class).newId("hello world");
        Context context = new Context(myId);

        UDPMessageService service = mInjector.getInstance(UDPMessageService.class);
        service.setContext(context);
        service.setSocketAddress(new InetSocketAddress(InetAddress.getAllByName("0.0.0.0")[0], 9000));
        service.start();

        Thread.sleep(5000);
        service.stop();
    }
}
