package ru.spbau.mit.networks.server;


import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.networks.client.MatrixProtobufMessage.Matrix;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class Worker implements Runnable {
    private final byte[] data;
    private final ServerNotifier notifier;

    public Worker(byte[] data, ServerNotifier notifier) {
        this.data = data;
        this.notifier = notifier;
    }

    @Override
    public void run() {
        byte[] result = null;
        try {
            final Matrix tmp = Matrix.parseFrom(
                    Arrays.copyOfRange(data, Integer.BYTES, data.length));
            final double[] doubles = tmp
                    .getDataList().stream()
                    .mapToDouble(Double::valueOf).toArray();
            final Jama.Matrix matrix = new Jama.Matrix(doubles, tmp.getRows());
            final Jama.Matrix invMatrix = matrix.inverse();

            final int rows = invMatrix.getRowDimension();
            int arraySize = Integer.BYTES + invMatrix.getRowDimension()
                    * invMatrix.getColumnDimension() * Integer.BYTES;
            result = new byte[arraySize];

            ByteBuffer buffer = ByteBuffer.wrap(result);
            buffer.putInt(arraySize - Integer.BYTES);
            for (double[] row: matrix.getArray()) {
                for (double v: row) {
                    buffer.putInt((int) v);
                }
            }
        } catch (InvalidProtocolBufferException ignored) {
        }
        notifier.notifyServer(result);
    }
}
