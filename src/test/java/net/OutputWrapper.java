package net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class OutputWrapper {

    public BufferedOutputStream out;

    public OutputWrapper(BufferedOutputStream out) {
        this.out = out;
    }

    public void writeBool(boolean b) throws IOException {
        out.write(b ? 1 : 0);
    }

    public void writeChar(char c) throws IOException {
        out.write(c);
    }

    public void writeInt(int n) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(n).array());
    }

    public void writeLong(long n) throws IOException {
        out.write(ByteBuffer.allocate(8).putLong(n).array());
    }

    public void writeDouble(double n) throws IOException {
        out.write(ByteBuffer.allocate(8).putDouble(n).array());
    }

    public void writeString(String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_16);
        writeInt(b.length);
        out.write(b);
    }

    public void writeBytes(byte[] b) throws IOException {
        writeInt(b.length);
        out.write(b);
    }

}
