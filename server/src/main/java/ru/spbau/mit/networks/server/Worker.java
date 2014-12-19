package ru.spbau.mit.networks.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.networks.server.MatrixProtobufMessage.Matrix;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
            final Jama.Matrix matrix = parseMatrix(data);
            final Jama.Matrix invMatrix = matrix.inverse();
            result = packMatrix(invMatrix);
        } catch (InvalidProtocolBufferException ignored) {
        }
        notifier.notifyServer(result);
    }

    private Jama.Matrix parseMatrix(byte[] data)
            throws InvalidProtocolBufferException {
        final Matrix tmp = Matrix.parseFrom(
                Arrays.copyOfRange(data, Integer.BYTES, data.length));
        final double[] doubles = tmp
                .getDataList().stream()
                .mapToDouble(d -> d).toArray();
        return new Jama.Matrix(doubles, tmp.getRows());
    }

    private byte[] packMatrix(Jama.Matrix matrix) {
        List<Double> doubles = new ArrayList<>(matrix.getRowDimension()
                * matrix.getColumnDimension());
        for (double d: matrix.getColumnPackedCopy()) {
            doubles.add(d);
        }
        byte[] matrixBytes = Matrix.newBuilder()
                .addAllData(doubles)
                .setRows(matrix.getRowDimension())
                .build().toByteArray();

        byte[] result = new byte[Integer.BYTES + matrixBytes.length];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putInt(matrixBytes.length);
        buffer.put(matrixBytes);

        return result;
    }
}
