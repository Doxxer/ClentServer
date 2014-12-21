package ru.spbau.mit.networks.server;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class ResizableByteBuffer {
    private int size = 0;
    private byte[] data = null;

    public void moveBytes(ByteBuffer buffer) {
        if (data == null) {
            makeBuffer(buffer);
            return;
        }
        final int remaining = buffer.remaining();
        reallocateIfNeeded(remaining);
        buffer.get(data, size, remaining);
        size += remaining;
    }

    public int getSize() {
        return size;
    }

    public byte[] getDataCopy() {
        return Arrays.copyOf(data, size);
    }

    public ByteBuffer getReadOnlyBuffer() {
        if (data == null) {
            return null;
        }
        return ByteBuffer.wrap(data, 0, size).asReadOnlyBuffer();
    }

    private void makeBuffer(ByteBuffer buffer) {
        size = buffer.remaining();
        data = new byte[buffer.capacity()];
        buffer.get(data, 0, size);
    }

    private void reallocateIfNeeded(int additionalSize) {
        if (additionalSize + size <= data.length) {
            return;
        }
        final int newLength = Math.max(
                additionalSize + size, (data.length * 3) / 2);
        data = Arrays.copyOf(data, newLength);
    }
}
