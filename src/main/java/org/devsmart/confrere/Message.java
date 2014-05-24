package org.devsmart.confrere;


import java.io.OutputStream;

public interface Message {

    /**
     * Serialize this message to an OutputStream. Note: this function should <b>never</b> call
     * close on the OutputStream because it is expected that the calling function will handle
     * that later.
     * @param out
     */
    void serialize(OutputStream out);
}
