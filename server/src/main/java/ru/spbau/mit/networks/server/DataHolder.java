package ru.spbau.mit.networks.server;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


public class DataHolder {
    private final Map<SocketChannel, ResizableByteBuffer> receivedData;
    private final Map<SocketChannel, ByteBuffer> writingData;
    private final ConcurrentLinkedQueue<Pair<SocketChannel, byte[]>> processed;

    {
        receivedData = new HashMap<>();
        writingData = new HashMap<>();
        processed = new ConcurrentLinkedQueue<>();
    }

    public void registerWriter(SocketChannel channel, byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).asReadOnlyBuffer();
        writingData.put(channel, buffer);
    }

    public ByteBuffer getWriterBuffer(SocketChannel channel) {
        ByteBuffer buffer = writingData.get(channel);
        assert buffer != null;
        return buffer;
    }

    public void unregisterWriter(SocketChannel channel) {
        writingData.remove(channel);
    }

    public Pair<SocketChannel, byte[]> getProcessedData() {
        return processed.poll();
    }

    public void registerReceiver(SocketChannel channel) {
        receivedData.put(channel, new ResizableByteBuffer());
    }

    public void unregisterReceiver(SocketChannel channel) {
        receivedData.remove(channel);
    }

    public byte[] extractReceivedData(SocketChannel channel) {
        byte[] bytes = getReceivedBuffer(channel).getDataCopy();
        unregisterReceiver(channel);
        return bytes;
    }

    public void moveReceivedData(SocketChannel channel, ByteBuffer buffer) {
        getReceivedBuffer(channel).moveBytes(buffer);
    }

    public Integer getFirstReceivedInteger(SocketChannel channel) {
        final ByteBuffer byteBuffer;
        byteBuffer = getReceivedBuffer(channel).getReadOnlyBuffer();
        if (byteBuffer == null || byteBuffer.remaining() < Integer.BYTES) {
            return null;
        }
        return byteBuffer.getInt();
    }

    public int getReceivedByteCount(SocketChannel channel) {
        return getReceivedBuffer(channel).getSize();
    }

    public ServerNotifier createNotifier(
            SocketChannel channel, Selector selector) {
        return data -> {
            processed.add(new Pair<>(channel, data));
            selector.wakeup();
        };
    }

    private ResizableByteBuffer getReceivedBuffer(SocketChannel channel) {
        ResizableByteBuffer resizableBuffer = receivedData.get(channel);
        assert resizableBuffer != null;
        return resizableBuffer;
    }
}
