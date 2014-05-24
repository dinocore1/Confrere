package org.devsmart.confrere;


import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class IdTypeAdapter extends TypeAdapter<Id>{
    @Override
    public void write(JsonWriter out, Id value) throws IOException {
        String hexString = Utils.bytesToHex(value.mData);
        out.value(hexString);
    }

    @Override
    public Id read(JsonReader in) throws IOException {
        String hexString = in.nextString();
        byte[] data = Utils.hexToBytes(hexString);
        return new Id(data);
    }
}
