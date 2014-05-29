package org.devsmart.confrere.services.udp;


import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Longs;

import java.net.InetSocketAddress;
import java.util.*;

public class ExternalAddresses {

    public static class ExternalAddress  {
        public final InetSocketAddress address;
        public long votes;

        static final Comparator<ExternalAddress> VotesDecend = new Comparator<ExternalAddress>() {
            @Override
            public int compare(ExternalAddress externalAddress, ExternalAddress externalAddress2) {
                return Longs.compare(externalAddress2.votes, externalAddress.votes);
            }
        };

        public ExternalAddress(InetSocketAddress address) {
            this.address = address;
            votes = 1;
        }
    }

    private LinkedList<ExternalAddress> mExternalAddresses = new LinkedList<ExternalAddress>();
    private final int mMaxAddresses;

    public ExternalAddresses(int maxAddresses){
        mMaxAddresses = maxAddresses;
    }

    public InetSocketAddress[] getBestAddresses() {
        Collections.sort(mExternalAddresses, ExternalAddress.VotesDecend);
        Collection<InetSocketAddress> retval = Collections2.transform(mExternalAddresses, new Function<ExternalAddress, InetSocketAddress>() {
            @Override
            public InetSocketAddress apply(ExternalAddress input) {
                return input.address;
            }
        });

        return retval.toArray(new InetSocketAddress[retval.size()]);
    }

    public void addExternalAddress(final InetSocketAddress externalAddress) {
        Optional<ExternalAddress> existing = Iterables.tryFind(mExternalAddresses, new Predicate<ExternalAddress>() {
            @Override
            public boolean apply(ExternalAddress input) {
                return input.address.equals(externalAddress);
            }
        });
        if(existing.isPresent()){
            existing.get().votes++;
        } else {
            ExternalAddress addr = new ExternalAddress(externalAddress);
            mExternalAddresses.add(addr);
        }

        if(mExternalAddresses.size() > mMaxAddresses){
            Collections.sort(mExternalAddresses, ExternalAddress.VotesDecend);
            while(mExternalAddresses.size() > mMaxAddresses){
                mExternalAddresses.removeLast();
            }
        }
    }
}
