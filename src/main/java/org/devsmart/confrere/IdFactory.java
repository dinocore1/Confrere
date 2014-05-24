package org.devsmart.confrere;


import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Random;

public class IdFactory {

    @Inject
    Random mRandom;

    @Inject
    Provider<MessageDigest> mHashProvider;

    public Id newRandomId() {
        byte[] data = new byte[Id.NUM_BYTES];
        mRandom.nextBytes(data);
        return new Id(data);
    }

    public Id newId(String value){
        try {
            MessageDigest hash = mHashProvider.get();
            byte[] data = hash.digest(value.getBytes("UTF-8"));
            return new Id(data);
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }
}
