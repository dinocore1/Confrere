package org.devsmart.confrere;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.util.Arrays;

public class Id {

    public static final int NUM_BYTES = 16;
    protected final byte[] mData = new byte[NUM_BYTES];

    public static Id Id(String value){
        byte[] data = IdFactory.HASH_FUNCTION.hashString(value, Charsets.UTF_8).asBytes();
        return new Id(data);
    }

    public Id(byte[] data){
        init(data, 0);
    }

    public Id(byte[] data, int offset) {
        init(data, offset);
    }

    private void init(byte[] data, int offset){
        if(data.length - offset < NUM_BYTES){
            throw new IllegalArgumentException(String.format("must be a valid %d-byte array", NUM_BYTES));
        }
        System.arraycopy(data,offset,mData,0,NUM_BYTES);
    }

    public BigInteger distance(Id other) {
        byte[] resultBytes = new byte[NUM_BYTES];
        for(int i=0;i<NUM_BYTES;i++){
            resultBytes[i] = (byte) (mData[i] ^ other.mData[i]);
        }
        return new BigInteger(1, resultBytes);
    }

    @Override
    public String toString() {
        String base64str = BaseEncoding.base64().encode(mData);
        return String.format("Id[%s]", base64str.substring(0, 5));
    }

    @Override
    public boolean equals(Object obj) {
        boolean retval = false;
        if(obj instanceof Id){
            retval = Arrays.equals(mData, ((Id) obj).mData);
        }
        return retval;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mData);
    }

    public void write(byte[] outdata, int offset) {
        System.arraycopy(mData, 0, outdata, offset, mData.length);
    }

    public byte[] getBytes() {
        byte[] retval = new byte[mData.length];
        write(retval, 0);
        return retval;
    }
}