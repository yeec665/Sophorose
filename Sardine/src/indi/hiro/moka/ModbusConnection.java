package indi.hiro.moka;

import java.util.Random;

/**
 * Created by Hiro on 2019/4/12.
 */
public class ModbusConnection extends SocketWrap {

    public static final int RB_FC_COILS = 0x01;
    public static final int RB_FC_DISCRETE_INPUTS = 0x02;

    public static final int RS_FC_HOLDING_REGISTERS = 0x03;
    public static final int RS_FC_INPUT_REGISTERS = 0x04;

    static final Random random = new Random();

    final byte transactionIdentifierH = (byte) random.nextInt(0x100);
    final byte transactionIdentifierL = (byte) random.nextInt(0x100);
    final byte unitIdentifier = (byte) random.nextInt(0x100);

    public static boolean[] readBits(String host, int port, int functionCode, int address, int length) throws Exception {
        ModbusConnection connection = new ModbusConnection(host, port);
        try {
            return connection.readBits(functionCode, address, length);
        } finally {
            connection.close();
        }
    }

    public static int[] readShorts(String host, int port, int functionCode, int address, int length) throws Exception {
        ModbusConnection connection = new ModbusConnection(host, port);
        try {
            return connection.readShorts(functionCode, address, length);
        } finally {
            connection.close();
        }
    }

    public ModbusConnection(String host, int port) throws Exception {
        super(host, port);
    }

    byte[] receiveModbus() throws Exception {
        byte[] header = receive(8);
        return receive(getShortAt(header, 4) - 2);
    }

    boolean[] readBits(int functionCode, int address, int length) throws Exception {
        send(new byte[]{
                transactionIdentifierH, transactionIdentifierL, 0x00, 0x00, 0x00, 0x06,
                unitIdentifier, (byte) functionCode, (byte) (address >> 8), (byte) address, (byte) (length >> 8), (byte) length
        });
        byte[] response = receiveModbus();
        boolean[] rv = new boolean[length];
        for (int i = 0; i < length; i++) {
            rv[i] = (response[1 + (i >> 3)] & (1 << (0x7 & i))) != 0;
        }
        return rv;
    }

    int[] readShorts(int functionCode, int address, int length) throws Exception {
        send(new byte[]{
                transactionIdentifierH, transactionIdentifierL, 0x00, 0x00, 0x00, 0x06,
                unitIdentifier, (byte) functionCode, (byte) (address >> 8), (byte) address, (byte) (length >> 8), (byte) length
        });
        byte[] response = receiveModbus();
        int[] rv = new int[length];
        for (int i = 0; i < length; i++) {
            rv[i] |= 0xFF00 & (response[1 + i * 2] << 8);
            rv[i] |= 0x00FF & response[2 + i * 2];
        }
        return rv;
    }
}
