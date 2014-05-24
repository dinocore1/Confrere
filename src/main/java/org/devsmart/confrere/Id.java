package org.devsmart.confrere;

import java.math.BigInteger;
import java.util.Arrays;

public class Id {

    public static final int NUM_BYTES = 20;
    protected final byte[] mData = new byte[NUM_BYTES];

    public Id(byte[] data){
        if(data == null || data.length != NUM_BYTES){
            throw new IllegalArgumentException(String.format("must be a valid %d-byte array", NUM_BYTES));
        } else {
            System.arraycopy(data,0,mData,0,NUM_BYTES);
        }
    }

    public BigInteger distance(Id other) {
        byte[] resultBytes = new byte[NUM_BYTES];
        for(int i=0;i<NUM_BYTES;i++){
            resultBytes[i] = (byte) (mData[i] ^ other.mData[i]);
        }
        return new BigInteger(1, resultBytes);
    }

    public String toHex() {
        return Utils.bytesToHex(mData);
    }

    @Override
    public String toString() {
        return String.format("Id[%s]", Utils.bytesToHex(mData, 0, 3));
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
}