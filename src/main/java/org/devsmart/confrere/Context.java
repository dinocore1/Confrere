package org.devsmart.confrere;

import com.google.common.eventbus.EventBus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class Context {

    public final ScheduledExecutorService mainThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable);
            t.setName("MainThread");
            return t;
        }
    });

    public final Id localId;
    public final PacketFactory packetFactory;
    public final EventBus eventBus;

    public Context(Id localId){
        this.eventBus = new EventBus();
        this.localId = localId;
        this.packetFactory = new PacketFactory(this);
    }


}
