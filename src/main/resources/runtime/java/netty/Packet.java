package com.zero.codegen.runtime.netty;

public class Packet {
    private int cmd;
    private PacketPayload payload;
    public Packet(){}
    public Packet(int cmd,byte[] payload){this(cmd, new ByteArrayPacketPayload(payload));}
    public Packet(int cmd, PacketPayload payload){this.cmd=cmd;this.payload=payload;}
    public int getCmd(){return cmd;}
    public byte[] getPayload(){return payload==null? null: payload.materialize();}
    public int getPayloadLength(){return payload==null? 0: payload.length();}
    PacketPayload getPayloadRef(){return payload;}
}

