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
            Jama.Matrix invMatrix = matrix.inverse();
            for (int i = 0; i < matrix.getRowDimension(); i++) {
                for (int j = 0; j < matrix.getColumnDimension(); j++) {
                    invMatrix = matrix.inverse();
                }
            }
            result = packDet(invMatrix.det());
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

    private byte[] packDet(double det) {
        byte[] result = new byte[Integer.BYTES + Double.BYTES];
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putInt(Double.BYTES);
        buffer.putDouble(det);
        return result;
    }
}
