package ru.spbau.mit.networks.client;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.networks.client.MatrixProtobufMessage.Matrix;

import java.util.ArrayList;
import java.util.Random;


public class MessageController {
    private byte[] clientData;
    private Random random = new Random();

    public byte[] createMessage() {
        int size = random.nextInt(100) + 200;
        ArrayList<Integer> data = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            data.add(random.nextInt(100));
        }

//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 5; i++) {
//            sb.append(i);
//        }
//        sb.append(System.lineSeparator());
//        return sb.toString().getBytes();

        clientData = Matrix.newBuilder()
                .addAllData(data)
                .setRows(size)
                .build().toByteArray();
        return clientData;
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
