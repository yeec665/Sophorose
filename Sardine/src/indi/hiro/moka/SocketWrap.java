package indi.hiro.moka;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Hiro on 2019/4/12.
 */
public abstract class SocketWrap {

    protected static int getByteAt(byte[] array, int offset) {
        return array[offset];
    }

    protected static void setShortAt(byte[] array, int offset, int value) {
        array[offset] = (byte) (value >> 8);
        array[offset + 1] = (byte) value;
    }

    protected static int getShortAt(byte[] array, int offset) {
        return 0xFF00 & (array[offset] << 8) | 0x00FF & (array[offset + 1]);
    }

    protected final Socket socket;

    public SocketWrap(String host, int port) throws Exception {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000);
    }

    public void send(byte[] data) throws Exception {
        OutputStream os = socket.getOutputStream();
        os.write(data);
        os.flush();
    }

    public byte[] receive(int length) throws Exception {
        InputStream is = socket.getInputStream();
        byte[] data = new byte[length];
        int ptr = 0;
        while (ptr < length) {
            int x = is.read(data, ptr, length - ptr);
            if (x == -1) {
                throw new EOFException();
            }
            ptr += x;
        }
        return data;
    }

    public void close() throws Exception {
        socket.close();
    }
}
