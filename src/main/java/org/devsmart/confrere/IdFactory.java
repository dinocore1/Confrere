package org.devsmart.confrere;


import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Random;

public class IdFactory {

    @Inject
    Random mRandom;

    public static final HashFunction HASH_FUNCTION = Hashing.sha1();

    public Id newRandomId() {
        byte[] data = new byte[Id.NUM_BYTES];
        mRandom.nextBytes(data);
        return new Id(data);
    }

    public Id newId(String value){
        byte[] data = HASH_FUNCTION.hashString(value, Charsets.UTF_8).asBytes();
        return new Id(data);
    }
}
