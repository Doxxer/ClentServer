package ru.spbau.mit.networks.client;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.networks.client.MatrixProtobufMessage.Matrix;

import java.util.ArrayList;

public class MessageGenerator {
    public byte[] createMessage() {
        int size = 3;
        ArrayList<Integer> data = new ArrayList<>();
        for (int i = 0; i < size * size; i++) {
            data.add(i);
        }

        byte[] matrix = Matrix.newBuilder()
                .addAllData(data)
                .setRows(size)
                .build().toByteArray();

//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < 5; i++) {
//            sb.append(i);
//        }
//        sb.append(System.lineSeparator());
//        return sb.toString().getBytes();

        return matrix;
    }

    public void decode(byte[] data) {
        try {
            Matrix matrix = Matrix.parseFrom(data);
            System.out.println(matrix);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
