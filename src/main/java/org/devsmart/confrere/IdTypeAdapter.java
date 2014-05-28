package org.devsmart.confrere;


import com.google.common.io.BaseEncoding;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class IdTypeAdapter extends TypeAdapter<Id>{

    BaseEncoding encoding = BaseEncoding.base64();

    @Override
    public void write(JsonWriter out, Id value) throws IOException {
        String b64String = encoding.encode(value.mData);
        out.value(b64String);
    }

    @Override
    public Id read(JsonReader in) throws IOException {
        String value = in.nextString();
        byte[] data = encoding.decode(value);
        return new Id(data);
    }
}
