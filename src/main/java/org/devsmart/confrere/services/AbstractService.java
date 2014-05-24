package org.devsmart.confrere.services;


import org.devsmart.confrere.Context;

public abstract class AbstractService {

    protected Context mContext;

    public AbstractService(Context context){
        mContext = context;
    }

    public abstract void start();
    public abstract void stop();
}
