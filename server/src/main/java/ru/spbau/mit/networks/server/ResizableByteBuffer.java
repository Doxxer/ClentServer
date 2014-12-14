package ru.spbau.mit.networks.server;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class ResizableByteBuffer {
    private int size = -1;
    private byte[] data = null;

    public void moveBytes(ByteBuffer buffer) {
        final int remaining = buffer.remaining();

        if (data == null) {
            size = remaining;
            data = new byte[buffer.capacity()];
            buffer.get(data, 0, size);
            return;
        }

        if (remaining + size > data.length) {
            final int newLength = Math.max(
                    remaining + size, (data.length * 3) / 2);
            data = Arrays.copyOf(data, newLength);
        }

        buffer.get(data, size, remaining);
        size += remaining;
    }

    public byte[] getDataCopy() {
        return Arrays.copyOf(data, size);
    }
}
