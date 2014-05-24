package org.devsmart.confrere;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Context {

    public final ScheduledExecutorService mainThread = Executors.newSingleThreadScheduledExecutor();

    public final Id localId;

    public Context(Id localId){
        this.localId = localId;
    }


}
