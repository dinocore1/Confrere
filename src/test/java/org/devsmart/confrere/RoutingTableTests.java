package org.devsmart.confrere;


import com.google.common.io.BaseEncoding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoutingTableTests {

    private Injector mInjector;

    @Before
    public void inject() {
        mInjector = Guice.createInjector(new DefaultModule());
    }


    private Id createId(String firstHex) {
        byte[] data = new byte[Id.NUM_BYTES];

        byte[] prefix = BaseEncoding.base16().decode(firstHex);
        System.arraycopy(prefix, 0, data, 0, prefix.length);

        return new Id(data);
    }

    @Test
    public void testRoutingTable() {
        RoutingTable table = new RoutingTable(createId("80"));

        int bucket = table.getBucket(createId("80"));
        assertEquals(Id.NUM_BYTES*8, bucket);

        bucket = table.getBucket(createId("40"));
        assertEquals(0, bucket);

        bucket = table.getBucket(createId("FF"));
        assertEquals(1, bucket);

        bucket = table.getBucket(createId("A0"));
        assertEquals(2, bucket);

        bucket = table.getBucket(createId("90"));
        assertEquals(3, bucket);

    }
}
