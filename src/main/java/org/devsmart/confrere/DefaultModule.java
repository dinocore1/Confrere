package org.devsmart.confrere;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.security.MessageDigest;


public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IdFactory.class).in(Singleton.class);
    }

    @Provides
    MessageDigest provideIdHashFunction() throws Exception{
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return sha1;
    }

}
