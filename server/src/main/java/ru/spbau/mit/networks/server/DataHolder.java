package ru.spbau.mit.networks.server;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;


public class DataHolder {
    private final Map<SocketChannel, ResizableByteBuffer> receivedData;
    private final Map<SocketChannel, ByteBuffer> writingData;
    private final Map<SocketChannel, Future<byte[]>> processingTasks;
    private final ConcurrentLinkedQueue<SocketChannel> processedTasks;

    {
        receivedData = new HashMap<>();
        writingData = new HashMap<>();
        processingTasks = new HashMap<>();
        processedTasks = new ConcurrentLinkedQueue<>();
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

    public void registerWorker(SocketChannel channel, Future<byte[]> future) {
        processingTasks.put(channel, future);
    }

    public Pair<byte[], SocketChannel> getProcessedData()
            throws WorkerException {
        List<SocketChannel> channels = new ArrayList<>();
        Future<byte[]> future = null;
        SocketChannel channel;

        while ((channel = processedTasks.poll()) != null) {
            future = processingTasks.get(channel);
            assert future != null;
            if (future.isDone()) {
                break;
            } else {
                future = null;
                channels.add(channel);
            }
        }

        processedTasks.addAll(channels);
        if (future == null) {
            return null;
        }
        processingTasks.remove(channel);
        try {
            return new Pair<>(future.get(), channel);
        } catch (Exception e) {
            throw new WorkerException(channel, e);
        }
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
        return () -> {
            processedTasks.add(channel);
            selector.wakeup();
        };
    }

    private ResizableByteBuffer getReceivedBuffer(SocketChannel channel) {
        ResizableByteBuffer resizableBuffer = receivedData.get(channel);
        assert resizableBuffer != null;
        return resizableBuffer;
    }
}
