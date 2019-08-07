package com.github.moos_ivp.moosbeans.comms;

public enum MessageType {
    Null((byte) '.'),
    Anonymous((byte) 'A'),
    Command((byte) 'C'),
    Poision((byte) 'K'),
    Notify((byte) 'N'),
    ServerRequest((byte) 'Q'),
    Register((byte) 'R'),
    Unregister((byte) 'U'),
    WildcardRegister((byte) '*'),
    WildcardUnregister((byte) '/'),
    Welcome((byte) 'W'),
    Data((byte) 'i'),
    NotSet((byte) '~'),
    Timing((byte) 'T'),
    TerminateConnection((byte) '^'),
    ServerRequestId((byte) -2);

    private final byte id;

    MessageType(byte id) {
        this.id = id;
    }

    public byte getValue() {
        return id;
    }

    public static MessageType fromByte(byte id) throws IllegalArgumentException {
        switch (id) {
            case '.':
                return Null;
            case 'A':
                return Anonymous;
            case 'C':
                return Command;
            case 'K':
                return Poision;
            case 'N':
                return Notify;
            case 'Q':
                return ServerRequest;
            case 'R':
                return Register;
            case 'U':
                return Unregister;
            case 'W':
                return Welcome;
            case 'i':
                return Data;
            case '~':
                return NotSet;
            case '*':
                return WildcardRegister;
            case '/':
                return WildcardUnregister;
            case 'T':
                return Timing;
            case '^':
                return TerminateConnection;
            case -2:
                return ServerRequestId;
            
            default:
                throw new IllegalArgumentException("Invalid MessageType: " + id);
        }
    }

    public static MessageType fromChar(char id) throws IllegalArgumentException {
        return fromByte((byte) id);
    }
}
