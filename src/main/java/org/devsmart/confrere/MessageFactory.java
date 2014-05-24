package org.devsmart.confrere;


import java.io.OutputStream;

public class MessageFactory {

    private enum Type {
        PING,
        POING
    }

    private class PingMessage implements Message {
        @Override
        public void serialize(OutputStream out) {

        }
    }

    private final Context mContext;

    public MessageFactory(Context context){
        mContext = context;
    }

    public Message newPing(){
        return new PingMessage();
    }

}
