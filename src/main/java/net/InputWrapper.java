package net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

public class InputWrapper {

    public BufferedInputStream in;

    public InputWrapper(BufferedInputStream in) {
        this.in = in;
    }

    public boolean readBool() throws IOException {
        int v = in.read();
        if (v == -1)
            throw new IOException("EOF");
        return v != 0;
    }

    public char readChar() throws IOException {
        int v = in.read();
        if (v == -1)
            throw new IOException("EOF");
        return (char) v;
    }

    public int readInt() throws IOException {
        byte[] b = in.readNBytes(4);
        if (b.length != 4)
            throw new IOException("EOF");
        return ByteBuffer.wrap(b).getInt();
    }

    public long readLong() throws IOException {
        byte[] b = in.readNBytes(8);
        if (b.length != 8)
            throw new IOException("EOF");
        return ByteBuffer.wrap(b).getLong();
    }

    public double readDouble() throws IOException {
        byte[] b = in.readNBytes(8);
        if (b.length != 8)
            throw new IOException("EOF");
        return ByteBuffer.wrap(b).getDouble();
    }

    public String readString() throws IOException {
        int size = readInt();
        byte[] b = in.readNBytes(size);
        if (b.length != size)
            throw new IOException("EOF");
        return new String(b, StandardCharsets.UTF_16);
    }

    public byte[] readBytes() throws IOException {
        int size = readInt();
        byte[] b = in.readNBytes(size);
        if (b.length != size)
            throw new IOException("EOF");
        return b;
    }

}
