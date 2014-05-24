package org.devsmart.confrere;


public class RoutingTable {

    public final Id id;

    public RoutingTable(Id id){
        this.id = id;
    }

    public int getBucket(Id id) {
        int i = 0;
        for(;i<Id.NUM_BYTES*8;i++){
            int bytenum = i/8;
            int bitmask = 1 << (7 - i%8);

            int q = (id.mData[bytenum] & bitmask) ^ (this.id.mData[bytenum] & bitmask);
            if(q != 0) {
                break;
            }
        }
        return i;
    }
}
