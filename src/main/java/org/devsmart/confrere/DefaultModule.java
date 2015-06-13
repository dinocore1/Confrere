package org.devsmart.confrere;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;


public class DefaultModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IdFactory.class).in(Singleton.class);
        bind(IdTypeAdapter.class).in(Singleton.class);
    }



    @Provides
    Gson provideGson(IdTypeAdapter idTypeAdapter) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Id.class, idTypeAdapter)
                .create();
        return gson;
    }


}
