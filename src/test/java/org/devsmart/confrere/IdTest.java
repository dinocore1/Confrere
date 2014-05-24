package org.devsmart.confrere;


import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class IdTest {

    private Injector mInjector;

    @Before
    public void inject() {
        mInjector = Guice.createInjector(new DefaultModule());
    }

    @Test
    public void serializeTest() {
        IdFactory factory = mInjector.getInstance(IdFactory.class);
        Id helloWorld = factory.newId("hello world");

        Gson gson = mInjector.getInstance(Gson.class);
        String serialized = gson.toJson(helloWorld);

        Id backagain = gson.fromJson(serialized, Id.class);

        assertEquals(helloWorld, backagain);
    }

    @Test
    public void testEquals() {
        IdFactory factory = mInjector.getInstance(IdFactory.class);

        Id helloWorld = factory.newId("hello world");

        Id helloWorld2 = factory.newId("hello world");
        assertEquals(helloWorld, helloWorld2);
    }

    @Test
    public void testToHex(){
        IdFactory factory = mInjector.getInstance(IdFactory.class);
        Id helloWorld = factory.newId("hello world");

        String hex = helloWorld.toHex();
        assertEquals("2AAE6C35C94FCFB415DBE95F408B9CE91EE846ED", hex);
    }

    @Test
    public void testDistance() {
        IdFactory factory = mInjector.getInstance(IdFactory.class);
        Id helloWorld = factory.newId("hello world");
        Id helloWorld2 = factory.newId("hello world");

        BigInteger distance = helloWorld.distance(helloWorld2);
        assertEquals(BigInteger.ZERO, distance);

        distance = helloWorld2.distance(helloWorld);
        assertEquals(BigInteger.ZERO, distance);

        Id junk = factory.newId("junk");
        distance = helloWorld.distance(junk);
        assertEquals(distance, junk.distance(helloWorld));
        assertNotEquals(BigInteger.ZERO, distance);

    }

}
