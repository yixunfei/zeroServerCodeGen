package com.zero.codegen.runtime.bytes;

public interface ByteCursor {
    void writeByte(byte v);
    void writeBytes(byte[] b, int off, int len);
    byte readByte();
    void readBytes(byte[] b, int off, int len);
    int readableBytes();
    int writableBytes();
    default byte[] array(){ throw new UnsupportedOperationException("array access is not supported"); }
    default int readerIndex(){ throw new UnsupportedOperationException("readerIndex access is not supported"); }
    void skip(int length);
}

