package MOOS.comms;

public enum DataType {
    Double((byte) 'D'), String((byte) 'S'), Binary((byte) 'B');

    private final byte id;

    DataType(byte id) {
        this.id = id;
    }

    public byte getValue() {
        return id;
    }

    public static DataType fromByte(byte id) throws IllegalArgumentException {
        switch (id) {
            case 'D':
                return Double;
            case 'S':
                return String;
            case 'B':
                return Binary;
            default:
                throw new IllegalArgumentException("Invalid DataType: " + id);
        }
    }

    public static DataType fromChar(char id) throws IllegalArgumentException {
        return fromByte((byte) (id));
    }

}
