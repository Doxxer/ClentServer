package ru.spbau.mit.networks.client;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.networks.client.MatrixProtobufMessage.Matrix;

import java.util.ArrayList;
import java.util.Random;


public class MessageController {
    private byte[] clientData = null;
    private Random random = new Random();


    public byte[] getMessage() {
        if (clientData == null) {
            createMessage();
        }
        return clientData;
    }

    public void createMessage() {
        int size = 50;
        ArrayList<Integer> data = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            data.add(random.nextInt(100));
        }

        clientData = Matrix.newBuilder()
                .addAllData(data)
                .setRows(size)
                .build().toByteArray();
    }

    public void decode(byte[] serverData) {
        try {
            Jama.Matrix serverMatrix = getMatrix(serverData);
            Jama.Matrix clientMatrix = getMatrix(clientData);

//            int size = serverMatrix.getRowDimension();
//            System.out.println(almostIdentity(serverMatrix.inverse().times(clientMatrix).minus(Jama.Matrix.identity(size, size))));

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
//            System.exit(1);
        }
    }

    private static boolean almostIdentity(Jama.Matrix a) {
        final double epsilon = 1e-8;
        int rows = a.getRowDimension();
        int columns = a.getColumnDimension();
        for (int c = 0; c < columns; c++) {
            for (int r = 0; r < rows; r++) {
                double ea = a.get(r, c);
                if (Math.abs(ea) >= epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    Jama.Matrix getMatrix(byte[] data) throws InvalidProtocolBufferException {
        Matrix matrix = Matrix.parseFrom(data);
        return new Jama.Matrix(matrix.getDataList().stream().mapToDouble(Double::valueOf).toArray(), matrix.getRows());
    }
}
