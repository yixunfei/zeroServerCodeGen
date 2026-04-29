package com.zero.codegen.benchmark.generated;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class CrossFrameworkCodecs {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper smileMapper = new ObjectMapper(new SmileFactory());
    private final Kryo kryo = new Kryo();

    public CrossFrameworkCodecs() {
        kryo.setRegistrationRequired(false);
    }

    public byte[] encodeJson(BenchmarkDtos.PayloadDto payload) throws Exception {
        return jsonMapper.writeValueAsBytes(payload);
    }

    public BenchmarkDtos.PayloadDto decodeJson(byte[] bytes) throws Exception {
        return jsonMapper.readValue(bytes, BenchmarkDtos.PayloadDto.class);
    }

    public byte[] encodeSmile(BenchmarkDtos.PayloadDto payload) throws Exception {
        return smileMapper.writeValueAsBytes(payload);
    }

    public BenchmarkDtos.PayloadDto decodeSmile(byte[] bytes) throws Exception {
        return smileMapper.readValue(bytes, BenchmarkDtos.PayloadDto.class);
    }

    public byte[] encodeKryo(BenchmarkDtos.PayloadDto payload) {
        Output output = new Output(4096, -1);
        kryo.writeObject(output, payload);
        output.flush();
        return output.toBytes();
    }

    public BenchmarkDtos.PayloadDto decodeKryo(byte[] bytes) {
        Input input = new Input(bytes);
        return kryo.readObject(input, BenchmarkDtos.PayloadDto.class);
    }

    public byte[] encodeJava(BenchmarkDtos.PayloadDto payload) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(4096);
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(payload);
        }
        return bytes.toByteArray();
    }

    public BenchmarkDtos.PayloadDto decodeJava(byte[] bytes) throws Exception {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (BenchmarkDtos.PayloadDto) input.readObject();
        }
    }

    public byte[] encodeProtobuf(BenchmarkDtos.PayloadDto payload) throws Exception {
        return ProtobufManualCodec.encodePayload(payload);
    }

    public BenchmarkDtos.PayloadDto decodeProtobuf(byte[] bytes) throws Exception {
        return ProtobufManualCodec.decodePayload(bytes);
    }
}
