package com.zero.codegen.benchmark.generated;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.WireFormat;
import org.agrona.DirectBuffer;
import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.capnproto.Data;
import org.capnproto.MessageBuilder;
import org.capnproto.MessageReader;
import org.capnproto.PrimitiveList;
import org.capnproto.SegmentBuilder;
import org.capnproto.SegmentReader;
import org.capnproto.Serialize;
import org.capnproto.StructBuilder;
import org.capnproto.StructFactory;
import org.capnproto.StructList;
import org.capnproto.StructReader;
import org.capnproto.StructSize;
import org.capnproto.Text;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TargetFrameworkCodecs {
    private static final ByteOrder LE = ByteOrder.LITTLE_ENDIAN;

    private TargetFrameworkCodecs() {
    }

    public static byte[] encodeProtobuf(CrossFrameworkDtos.Frame frame) throws Exception {
        return writeProto(out -> {
            out.writeInt64(1, frame.battleId);
            out.writeInt64(2, frame.serverTime);
            out.writeInt32(3, frame.frameNo);
            out.writeInt32(4, frame.roomId);
            out.writeInt32(5, frame.checksum);
            out.writeFloat(6, frame.actorX);
            out.writeFloat(7, frame.actorY);
            out.writeFloat(8, frame.actorZ);
            out.writeEnum(9, frame.state.ordinal());
            out.writeBool(10, frame.replay);
            out.writeString(11, frame.remark);
            for (int slot : frame.changedSlots) {
                out.writeInt32(12, slot);
            }
            out.writeByteArray(13, frame.payload);
            for (CrossFrameworkDtos.Skill skill : frame.casts) {
                out.writeByteArray(14, encodeProtoSkill(skill));
            }
            for (CrossFrameworkDtos.Buff buff : frame.buffs) {
                out.writeByteArray(15, encodeProtoBuff(buff));
            }
        });
    }

    public static CrossFrameworkDtos.Frame decodeProtobuf(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        CrossFrameworkDtos.Frame frame = new CrossFrameworkDtos.Frame();
        List<Integer> slots = new ArrayList<>();
        frame.state = CrossFrameworkDtos.State.IDLE;
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> frame.battleId = input.readInt64();
                case 2 -> frame.serverTime = input.readInt64();
                case 3 -> frame.frameNo = input.readInt32();
                case 4 -> frame.roomId = input.readInt32();
                case 5 -> frame.checksum = input.readInt32();
                case 6 -> frame.actorX = input.readFloat();
                case 7 -> frame.actorY = input.readFloat();
                case 8 -> frame.actorZ = input.readFloat();
                case 9 -> frame.state = CrossFrameworkDtos.State.values()[input.readEnum()];
                case 10 -> frame.replay = input.readBool();
                case 11 -> frame.remark = input.readString();
                case 12 -> slots.add(input.readInt32());
                case 13 -> frame.payload = input.readByteArray();
                case 14 -> frame.casts.add(decodeProtoSkill(input.readByteArray()));
                case 15 -> frame.buffs.add(decodeProtoBuff(input.readByteArray()));
                default -> input.skipField(tag);
            }
        }
        frame.changedSlots = slots.stream().mapToInt(Integer::intValue).toArray();
        return frame;
    }

    private static byte[] encodeProtoSkill(CrossFrameworkDtos.Skill skill) throws Exception {
        return writeProto(out -> {
            out.writeInt32(1, skill.skillId);
            out.writeInt32(2, skill.damage);
            out.writeBool(3, skill.critical);
            out.writeFloat(4, skill.ratio);
        });
    }

    private static CrossFrameworkDtos.Skill decodeProtoSkill(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        CrossFrameworkDtos.Skill skill = new CrossFrameworkDtos.Skill();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> skill.skillId = input.readInt32();
                case 2 -> skill.damage = input.readInt32();
                case 3 -> skill.critical = input.readBool();
                case 4 -> skill.ratio = input.readFloat();
                default -> input.skipField(tag);
            }
        }
        return skill;
    }

    private static byte[] encodeProtoBuff(CrossFrameworkDtos.Buff buff) throws Exception {
        return writeProto(out -> {
            out.writeInt32(1, buff.slot);
            out.writeInt32(2, buff.buffId);
            out.writeInt32(3, buff.level);
            out.writeInt64(4, buff.expireAt);
            out.writeBool(5, buff.positive);
        });
    }

    private static CrossFrameworkDtos.Buff decodeProtoBuff(byte[] bytes) throws Exception {
        CodedInputStream input = CodedInputStream.newInstance(bytes);
        CrossFrameworkDtos.Buff buff = new CrossFrameworkDtos.Buff();
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            switch (WireFormat.getTagFieldNumber(tag)) {
                case 1 -> buff.slot = input.readInt32();
                case 2 -> buff.buffId = input.readInt32();
                case 3 -> buff.level = input.readInt32();
                case 4 -> buff.expireAt = input.readInt64();
                case 5 -> buff.positive = input.readBool();
                default -> input.skipField(tag);
            }
        }
        return buff;
    }

    private static byte[] writeProto(ProtoWriter writer) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(1024);
        CodedOutputStream output = CodedOutputStream.newInstance(bytes);
        writer.write(output);
        output.flush();
        return bytes.toByteArray();
    }

    public static byte[] encodeSbe(CrossFrameworkDtos.Frame frame) {
        ExpandableArrayBuffer buffer = new ExpandableArrayBuffer(Math.max(256, estimateSbeSize(frame)));
        int offset = 0;
        buffer.putLong(offset, frame.battleId, LE); offset += 8;
        buffer.putLong(offset, frame.serverTime, LE); offset += 8;
        buffer.putInt(offset, frame.frameNo, LE); offset += 4;
        buffer.putInt(offset, frame.roomId, LE); offset += 4;
        buffer.putInt(offset, frame.checksum, LE); offset += 4;
        buffer.putFloat(offset, frame.actorX, LE); offset += 4;
        buffer.putFloat(offset, frame.actorY, LE); offset += 4;
        buffer.putFloat(offset, frame.actorZ, LE); offset += 4;
        buffer.putInt(offset, frame.state.ordinal(), LE); offset += 4;
        buffer.putByte(offset++, (byte) (frame.replay ? 1 : 0));
        offset = putBytes(buffer, offset, frame.remark.getBytes(StandardCharsets.UTF_8));
        offset = putIntArray(buffer, offset, frame.changedSlots);
        offset = putBytes(buffer, offset, frame.payload);
        buffer.putInt(offset, frame.casts.size(), LE); offset += 4;
        for (CrossFrameworkDtos.Skill skill : frame.casts) {
            buffer.putInt(offset, skill.skillId, LE); offset += 4;
            buffer.putInt(offset, skill.damage, LE); offset += 4;
            buffer.putByte(offset++, (byte) (skill.critical ? 1 : 0));
            buffer.putFloat(offset, skill.ratio, LE); offset += 4;
        }
        buffer.putInt(offset, frame.buffs.size(), LE); offset += 4;
        for (CrossFrameworkDtos.Buff buff : frame.buffs) {
            buffer.putInt(offset, buff.slot, LE); offset += 4;
            buffer.putInt(offset, buff.buffId, LE); offset += 4;
            buffer.putInt(offset, buff.level, LE); offset += 4;
            buffer.putLong(offset, buff.expireAt, LE); offset += 8;
            buffer.putByte(offset++, (byte) (buff.positive ? 1 : 0));
        }
        byte[] bytes = new byte[offset];
        buffer.getBytes(0, bytes);
        return bytes;
    }

    public static CrossFrameworkDtos.Frame decodeSbe(byte[] bytes) {
        UnsafeBuffer buffer = new UnsafeBuffer(bytes);
        CrossFrameworkDtos.Frame frame = new CrossFrameworkDtos.Frame();
        int offset = 0;
        frame.battleId = buffer.getLong(offset, LE); offset += 8;
        frame.serverTime = buffer.getLong(offset, LE); offset += 8;
        frame.frameNo = buffer.getInt(offset, LE); offset += 4;
        frame.roomId = buffer.getInt(offset, LE); offset += 4;
        frame.checksum = buffer.getInt(offset, LE); offset += 4;
        frame.actorX = buffer.getFloat(offset, LE); offset += 4;
        frame.actorY = buffer.getFloat(offset, LE); offset += 4;
        frame.actorZ = buffer.getFloat(offset, LE); offset += 4;
        frame.state = CrossFrameworkDtos.State.values()[buffer.getInt(offset, LE)]; offset += 4;
        frame.replay = buffer.getByte(offset++) != 0;
        BytesWithOffset remark = getBytes(buffer, offset); offset = remark.nextOffset;
        frame.remark = new String(remark.bytes, StandardCharsets.UTF_8);
        IntArrayWithOffset slots = getIntArray(buffer, offset); offset = slots.nextOffset;
        frame.changedSlots = slots.values;
        BytesWithOffset payload = getBytes(buffer, offset); offset = payload.nextOffset;
        frame.payload = payload.bytes;
        int castCount = buffer.getInt(offset, LE); offset += 4;
        for (int i = 0; i < castCount; i++) {
            CrossFrameworkDtos.Skill skill = new CrossFrameworkDtos.Skill();
            skill.skillId = buffer.getInt(offset, LE); offset += 4;
            skill.damage = buffer.getInt(offset, LE); offset += 4;
            skill.critical = buffer.getByte(offset++) != 0;
            skill.ratio = buffer.getFloat(offset, LE); offset += 4;
            frame.casts.add(skill);
        }
        int buffCount = buffer.getInt(offset, LE); offset += 4;
        for (int i = 0; i < buffCount; i++) {
            CrossFrameworkDtos.Buff buff = new CrossFrameworkDtos.Buff();
            buff.slot = buffer.getInt(offset, LE); offset += 4;
            buff.buffId = buffer.getInt(offset, LE); offset += 4;
            buff.level = buffer.getInt(offset, LE); offset += 4;
            buff.expireAt = buffer.getLong(offset, LE); offset += 8;
            buff.positive = buffer.getByte(offset++) != 0;
            frame.buffs.add(buff);
        }
        return frame;
    }

    public static byte[] encodeFlatBuffers(CrossFrameworkDtos.Frame frame) {
        FlatBufferBuilder builder = new FlatBufferBuilder(Math.max(256, estimateSbeSize(frame) + 128));
        int remark = builder.createString(frame.remark);
        int payload = builder.createByteVector(frame.payload);
        int changedSlots = createIntVector(builder, frame.changedSlots);
        int[] castOffsets = new int[frame.casts.size()];
        for (int i = 0; i < castOffsets.length; i++) {
            castOffsets[i] = FbSkill.create(builder, frame.casts.get(i));
        }
        int casts = builder.createVectorOfTables(castOffsets);
        int[] buffOffsets = new int[frame.buffs.size()];
        for (int i = 0; i < buffOffsets.length; i++) {
            buffOffsets[i] = FbBuff.create(builder, frame.buffs.get(i));
        }
        int buffs = builder.createVectorOfTables(buffOffsets);
        builder.startTable(15);
        builder.addLong(0, frame.battleId, 0);
        builder.addLong(1, frame.serverTime, 0);
        builder.addInt(2, frame.frameNo, 0);
        builder.addInt(3, frame.roomId, 0);
        builder.addInt(4, frame.checksum, 0);
        builder.addFloat(5, frame.actorX, 0);
        builder.addFloat(6, frame.actorY, 0);
        builder.addFloat(7, frame.actorZ, 0);
        builder.addInt(8, frame.state.ordinal(), 0);
        builder.addBoolean(9, frame.replay, false);
        builder.addOffset(10, remark, 0);
        builder.addOffset(11, changedSlots, 0);
        builder.addOffset(12, payload, 0);
        builder.addOffset(13, casts, 0);
        builder.addOffset(14, buffs, 0);
        int root = builder.endTable();
        builder.finish(root);
        return builder.sizedByteArray();
    }

    public static CrossFrameworkDtos.Frame decodeFlatBuffers(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        FbFrame root = FbFrame.getRoot(bb);
        CrossFrameworkDtos.Frame frame = new CrossFrameworkDtos.Frame();
        frame.battleId = root.battleId();
        frame.serverTime = root.serverTime();
        frame.frameNo = root.frameNo();
        frame.roomId = root.roomId();
        frame.checksum = root.checksum();
        frame.actorX = root.actorX();
        frame.actorY = root.actorY();
        frame.actorZ = root.actorZ();
        frame.state = CrossFrameworkDtos.State.values()[root.state()];
        frame.replay = root.replay();
        frame.remark = root.remark();
        frame.changedSlots = root.changedSlotsArray();
        frame.payload = root.payloadArray();
        FbSkill skill = new FbSkill();
        for (int i = 0; i < root.castsLength(); i++) {
            root.casts(skill, i);
            CrossFrameworkDtos.Skill dto = new CrossFrameworkDtos.Skill();
            dto.skillId = skill.skillId();
            dto.damage = skill.damage();
            dto.critical = skill.critical();
            dto.ratio = skill.ratio();
            frame.casts.add(dto);
        }
        FbBuff buff = new FbBuff();
        for (int i = 0; i < root.buffsLength(); i++) {
            root.buffs(buff, i);
            CrossFrameworkDtos.Buff dto = new CrossFrameworkDtos.Buff();
            dto.slot = buff.slot();
            dto.buffId = buff.buffId();
            dto.level = buff.level();
            dto.expireAt = buff.expireAt();
            dto.positive = buff.positive();
            frame.buffs.add(dto);
        }
        return frame;
    }

    public static long readFlatBuffersRandomAccess(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        FbFrame root = FbFrame.getRoot(bb);
        long value = root.battleId() ^ root.serverTime();
        value += root.checksum();
        value += root.remarkLength();
        int payloadLength = root.payloadLength();
        if (payloadLength > 0) {
            value += root.payloadByte(payloadLength / 2);
        }
        int slotLength = root.changedSlotsLength();
        if (slotLength > 0) {
            value += root.changedSlot(slotLength / 2);
        }
        int castLength = root.castsLength();
        if (castLength > 0) {
            FbSkill skill = new FbSkill();
            root.casts(skill, castLength / 2);
            value += skill.skillId() + skill.damage() + Float.floatToIntBits(skill.ratio());
        }
        int buffLength = root.buffsLength();
        if (buffLength > 0) {
            FbBuff buff = new FbBuff();
            root.buffs(buff, buffLength / 2);
            value += buff.buffId() + buff.level() + buff.expireAt();
        }
        value += root.state() + (root.replay() ? 17 : 3);
        return value;
    }

    public static byte[] encodeCapnProto(CrossFrameworkDtos.Frame frame) throws Exception {
        MessageBuilder message = new MessageBuilder(Math.max(128, estimateSbeSize(frame) / 8 + 64));
        CapFrame.Builder root = message.initRoot(CapFrame.factory);
        root.setBattleId(frame.battleId);
        root.setServerTime(frame.serverTime);
        root.setFrameNo(frame.frameNo);
        root.setRoomId(frame.roomId);
        root.setChecksum(frame.checksum);
        root.setActorX(frame.actorX);
        root.setActorY(frame.actorY);
        root.setActorZ(frame.actorZ);
        root.setState(frame.state.ordinal());
        root.setReplay(frame.replay);
        root.setRemark(frame.remark);
        PrimitiveList.Int.Builder slots = root.initChangedSlots(frame.changedSlots.length);
        for (int i = 0; i < frame.changedSlots.length; i++) {
            slots.set(i, frame.changedSlots[i]);
        }
        root.setPayload(frame.payload);
        StructList.Builder<CapSkill.Builder> casts = root.initCasts(frame.casts.size());
        for (int i = 0; i < frame.casts.size(); i++) {
            CapSkill.Builder skill = casts.get(i);
            CrossFrameworkDtos.Skill source = frame.casts.get(i);
            skill.setSkillId(source.skillId);
            skill.setDamage(source.damage);
            skill.setCritical(source.critical);
            skill.setRatio(source.ratio);
        }
        StructList.Builder<CapBuff.Builder> buffs = root.initBuffs(frame.buffs.size());
        for (int i = 0; i < frame.buffs.size(); i++) {
            CapBuff.Builder buff = buffs.get(i);
            CrossFrameworkDtos.Buff source = frame.buffs.get(i);
            buff.setSlot(source.slot);
            buff.setBuffId(source.buffId);
            buff.setLevel(source.level);
            buff.setExpireAt(source.expireAt);
            buff.setPositive(source.positive);
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream(estimateSbeSize(frame) + 128);
        Serialize.write(Channels.newChannel(output), message);
        return output.toByteArray();
    }

    public static CrossFrameworkDtos.Frame decodeCapnProto(byte[] bytes) throws Exception {
        MessageReader message = Serialize.read(ByteBuffer.wrap(bytes));
        CapFrame.Reader root = message.getRoot(CapFrame.factory);
        CrossFrameworkDtos.Frame frame = new CrossFrameworkDtos.Frame();
        frame.battleId = root.battleId();
        frame.serverTime = root.serverTime();
        frame.frameNo = root.frameNo();
        frame.roomId = root.roomId();
        frame.checksum = root.checksum();
        frame.actorX = root.actorX();
        frame.actorY = root.actorY();
        frame.actorZ = root.actorZ();
        frame.state = CrossFrameworkDtos.State.values()[root.state()];
        frame.replay = root.replay();
        frame.remark = root.remark();
        PrimitiveList.Int.Reader slots = root.changedSlots();
        frame.changedSlots = new int[slots.size()];
        for (int i = 0; i < frame.changedSlots.length; i++) {
            frame.changedSlots[i] = slots.get(i);
        }
        frame.payload = root.payload();
        StructList.Reader<CapSkill.Reader> casts = root.casts();
        for (int i = 0; i < casts.size(); i++) {
            CapSkill.Reader skill = casts.get(i);
            CrossFrameworkDtos.Skill dto = new CrossFrameworkDtos.Skill();
            dto.skillId = skill.skillId();
            dto.damage = skill.damage();
            dto.critical = skill.critical();
            dto.ratio = skill.ratio();
            frame.casts.add(dto);
        }
        StructList.Reader<CapBuff.Reader> buffs = root.buffs();
        for (int i = 0; i < buffs.size(); i++) {
            CapBuff.Reader buff = buffs.get(i);
            CrossFrameworkDtos.Buff dto = new CrossFrameworkDtos.Buff();
            dto.slot = buff.slot();
            dto.buffId = buff.buffId();
            dto.level = buff.level();
            dto.expireAt = buff.expireAt();
            dto.positive = buff.positive();
            frame.buffs.add(dto);
        }
        return frame;
    }

    public static long readCapnProtoRandomAccess(byte[] bytes) throws Exception {
        MessageReader message = Serialize.read(ByteBuffer.wrap(bytes));
        CapFrame.Reader root = message.getRoot(CapFrame.factory);
        long value = root.battleId() ^ root.serverTime();
        value += root.checksum();
        value += root.remarkReader().size();
        Data.Reader payload = root.payloadReader();
        if (payload.size() > 0) {
            value += payload.buffer.get(payload.offset + payload.size() / 2);
        }
        PrimitiveList.Int.Reader slots = root.changedSlots();
        if (slots.size() > 0) {
            value += slots.get(slots.size() / 2);
        }
        StructList.Reader<CapSkill.Reader> casts = root.casts();
        if (casts.size() > 0) {
            CapSkill.Reader skill = casts.get(casts.size() / 2);
            value += skill.skillId() + skill.damage() + Float.floatToIntBits(skill.ratio());
        }
        StructList.Reader<CapBuff.Reader> buffs = root.buffs();
        if (buffs.size() > 0) {
            CapBuff.Reader buff = buffs.get(buffs.size() / 2);
            value += buff.buffId() + buff.level() + buff.expireAt();
        }
        value += root.state() + (root.replay() ? 17 : 3);
        return value;
    }

    public static byte[] writeCapnProtoRandomAccess(CrossFrameworkDtos.Frame frame) throws Exception {
        MessageBuilder message = new MessageBuilder(Math.max(128, estimateSbeSize(frame) / 8 + 64));
        CapFrame.Builder root = message.initRoot(CapFrame.factory);

        root.setRemark(frame.remark);
        StructList.Builder<CapBuff.Builder> buffs = root.initBuffs(frame.buffs.size());
        for (int i = 0; i < frame.buffs.size(); i++) {
            int index = permute(i, frame.buffs.size(), 31);
            CrossFrameworkDtos.Buff source = frame.buffs.get(index);
            CapBuff.Builder buff = buffs.get(index);
            buff.setPositive(source.positive);
            buff.setExpireAt(source.expireAt);
            buff.setLevel(source.level);
            buff.setBuffId(source.buffId);
            buff.setSlot(source.slot);
        }
        StructList.Builder<CapSkill.Builder> casts = root.initCasts(frame.casts.size());
        for (int i = 0; i < frame.casts.size(); i++) {
            int index = permute(i, frame.casts.size(), 17);
            CrossFrameworkDtos.Skill source = frame.casts.get(index);
            CapSkill.Builder skill = casts.get(index);
            skill.setRatio(source.ratio);
            skill.setCritical(source.critical);
            skill.setDamage(source.damage);
            skill.setSkillId(source.skillId);
        }
        root.setPayload(frame.payload);
        PrimitiveList.Int.Builder slots = root.initChangedSlots(frame.changedSlots.length);
        for (int i = 0; i < frame.changedSlots.length; i++) {
            int index = permute(i, frame.changedSlots.length, 17);
            slots.set(index, frame.changedSlots[index]);
        }
        root.setReplay(frame.replay);
        root.setState(frame.state.ordinal());
        root.setActorZ(frame.actorZ);
        root.setActorY(frame.actorY);
        root.setActorX(frame.actorX);
        root.setChecksum(frame.checksum);
        root.setRoomId(frame.roomId);
        root.setFrameNo(frame.frameNo);
        root.setServerTime(frame.serverTime);
        root.setBattleId(frame.battleId);

        ByteArrayOutputStream output = new ByteArrayOutputStream(estimateSbeSize(frame) + 128);
        Serialize.write(Channels.newChannel(output), message);
        return output.toByteArray();
    }

    private static int estimateSbeSize(CrossFrameworkDtos.Frame frame) {
        int size = 45;
        size += 4 + frame.remark.getBytes(StandardCharsets.UTF_8).length;
        size += 4 + frame.changedSlots.length * 4;
        size += 4 + frame.payload.length;
        size += 4 + frame.casts.size() * 13;
        size += 4 + frame.buffs.size() * 21;
        return size;
    }

    private static int putBytes(MutableDirectBuffer buffer, int offset, byte[] bytes) {
        buffer.putInt(offset, bytes.length, LE);
        buffer.putBytes(offset + 4, bytes);
        return offset + 4 + bytes.length;
    }

    private static int putIntArray(MutableDirectBuffer buffer, int offset, int[] values) {
        buffer.putInt(offset, values.length, LE);
        offset += 4;
        for (int value : values) {
            buffer.putInt(offset, value, LE);
            offset += 4;
        }
        return offset;
    }

    private static BytesWithOffset getBytes(DirectBuffer buffer, int offset) {
        int length = buffer.getInt(offset, LE);
        byte[] bytes = new byte[length];
        buffer.getBytes(offset + 4, bytes);
        return new BytesWithOffset(bytes, offset + 4 + length);
    }

    private static IntArrayWithOffset getIntArray(DirectBuffer buffer, int offset) {
        int length = buffer.getInt(offset, LE);
        int[] values = new int[length];
        offset += 4;
        for (int i = 0; i < length; i++) {
            values[i] = buffer.getInt(offset, LE);
            offset += 4;
        }
        return new IntArrayWithOffset(values, offset);
    }

    private static int createIntVector(FlatBufferBuilder builder, int[] values) {
        builder.startVector(4, values.length, 4);
        for (int i = values.length - 1; i >= 0; i--) {
            builder.addInt(values[i]);
        }
        return builder.endVector();
    }

    private static int permute(int index, int size, int multiplier) {
        return size == 0 ? 0 : (index * multiplier) % size;
    }

    private interface ProtoWriter {
        void write(CodedOutputStream output) throws Exception;
    }

    private record BytesWithOffset(byte[] bytes, int nextOffset) {
    }

    private record IntArrayWithOffset(int[] values, int nextOffset) {
    }

    private static final class FbSkill extends Table {
        static int create(FlatBufferBuilder builder, CrossFrameworkDtos.Skill skill) {
            builder.startTable(4);
            builder.addInt(0, skill.skillId, 0);
            builder.addInt(1, skill.damage, 0);
            builder.addBoolean(2, skill.critical, false);
            builder.addFloat(3, skill.ratio, 0);
            return builder.endTable();
        }

        void assign(int pos, ByteBuffer buffer) {
            __reset(pos, buffer);
        }

        int skillId() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        int damage() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        boolean critical() { int o = __offset(8); return o != 0 && bb.get(o + bb_pos) != 0; }
        float ratio() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0F; }
    }

    private static final class FbBuff extends Table {
        static int create(FlatBufferBuilder builder, CrossFrameworkDtos.Buff buff) {
            builder.startTable(5);
            builder.addInt(0, buff.slot, 0);
            builder.addInt(1, buff.buffId, 0);
            builder.addInt(2, buff.level, 0);
            builder.addLong(3, buff.expireAt, 0);
            builder.addBoolean(4, buff.positive, false);
            return builder.endTable();
        }

        void assign(int pos, ByteBuffer buffer) {
            __reset(pos, buffer);
        }

        int slot() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        int buffId() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        int level() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        long expireAt() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
        boolean positive() { int o = __offset(12); return o != 0 && bb.get(o + bb_pos) != 0; }
    }

    private static final class FbFrame extends Table {
        static FbFrame getRoot(ByteBuffer buffer) {
            FbFrame frame = new FbFrame();
            frame.__reset(buffer.getInt(buffer.position()) + buffer.position(), buffer);
            return frame;
        }

        long battleId() { int o = __offset(4); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
        long serverTime() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
        int frameNo() { int o = __offset(8); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        int roomId() { int o = __offset(10); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        int checksum() { int o = __offset(12); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        float actorX() { int o = __offset(14); return o != 0 ? bb.getFloat(o + bb_pos) : 0F; }
        float actorY() { int o = __offset(16); return o != 0 ? bb.getFloat(o + bb_pos) : 0F; }
        float actorZ() { int o = __offset(18); return o != 0 ? bb.getFloat(o + bb_pos) : 0F; }
        int state() { int o = __offset(20); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
        boolean replay() { int o = __offset(22); return o != 0 && bb.get(o + bb_pos) != 0; }
        String remark() { int o = __offset(24); return o != 0 ? __string(o + bb_pos) : ""; }
        int remarkLength() {
            int o = __offset(24);
            if (o == 0) return 0;
            int start = o + bb_pos;
            start += bb.getInt(start);
            return bb.getInt(start);
        }

        int[] changedSlotsArray() {
            int o = __offset(26);
            if (o == 0) return new int[0];
            int len = __vector_len(o);
            int start = __vector(o);
            int[] values = new int[len];
            for (int i = 0; i < len; i++) values[i] = bb.getInt(start + i * 4);
            return values;
        }
        int changedSlotsLength() { int o = __offset(26); return o != 0 ? __vector_len(o) : 0; }
        int changedSlot(int index) {
            int o = __offset(26);
            return bb.getInt(__vector(o) + index * 4);
        }

        byte[] payloadArray() {
            int o = __offset(28);
            if (o == 0) return new byte[0];
            int len = __vector_len(o);
            int start = __vector(o);
            byte[] values = new byte[len];
            ByteBuffer dup = bb.duplicate();
            dup.position(start);
            dup.get(values);
            return values;
        }
        int payloadLength() { int o = __offset(28); return o != 0 ? __vector_len(o) : 0; }
        byte payloadByte(int index) {
            int o = __offset(28);
            return bb.get(__vector(o) + index);
        }

        int castsLength() { int o = __offset(30); return o != 0 ? __vector_len(o) : 0; }
        void casts(FbSkill skill, int index) {
            int o = __offset(30);
            int start = __vector(o);
            skill.assign(__indirect(start + index * 4), bb);
        }

        int buffsLength() { int o = __offset(32); return o != 0 ? __vector_len(o) : 0; }
        void buffs(FbBuff buff, int index) {
            int o = __offset(32);
            int start = __vector(o);
            buff.assign(__indirect(start + index * 4), bb);
        }
    }

    private static final class CapSkill {
        static final Factory factory = new Factory();

        static final class Factory extends StructFactory<Builder, Reader> {
            public Builder constructBuilder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) {
                return new Builder(segment, data, pointers, dataSize, pointerCount);
            }
            public Reader constructReader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
                return new Reader(segment, data, pointers, dataSize, pointerCount, nestingLimit);
            }
            public StructSize structSize() { return new StructSize((short) 2, (short) 0); }
            public Reader asReader(Builder builder) { return new Reader(); }
        }
        static final class Builder extends StructBuilder {
            Builder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) { super(segment, data, pointers, dataSize, pointerCount); }
            void setSkillId(int value) { _setIntField(0, value); }
            void setDamage(int value) { _setIntField(1, value); }
            void setCritical(boolean value) { _setBooleanField(96, value); }
            void setRatio(float value) { _setFloatField(2, value); }
        }
        static final class Reader extends StructReader {
            Reader() { super(); }
            Reader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) { super(segment, data, pointers, dataSize, pointerCount, nestingLimit); }
            int skillId() { return _getIntField(0); }
            int damage() { return _getIntField(1); }
            boolean critical() { return _getBooleanField(96); }
            float ratio() { return _getFloatField(2); }
        }
    }

    private static final class CapBuff {
        static final Factory factory = new Factory();

        static final class Factory extends StructFactory<Builder, Reader> {
            public Builder constructBuilder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) {
                return new Builder(segment, data, pointers, dataSize, pointerCount);
            }
            public Reader constructReader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
                return new Reader(segment, data, pointers, dataSize, pointerCount, nestingLimit);
            }
            public StructSize structSize() { return new StructSize((short) 4, (short) 0); }
            public Reader asReader(Builder builder) { return new Reader(); }
        }
        static final class Builder extends StructBuilder {
            Builder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) { super(segment, data, pointers, dataSize, pointerCount); }
            void setSlot(int value) { _setIntField(0, value); }
            void setBuffId(int value) { _setIntField(1, value); }
            void setLevel(int value) { _setIntField(2, value); }
            void setExpireAt(long value) { _setLongField(2, value); }
            void setPositive(boolean value) { _setBooleanField(192, value); }
        }
        static final class Reader extends StructReader {
            Reader() { super(); }
            Reader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) { super(segment, data, pointers, dataSize, pointerCount, nestingLimit); }
            int slot() { return _getIntField(0); }
            int buffId() { return _getIntField(1); }
            int level() { return _getIntField(2); }
            long expireAt() { return _getLongField(2); }
            boolean positive() { return _getBooleanField(192); }
        }
    }

    private static final class CapFrame {
        static final Factory factory = new Factory();
        static final StructList.Factory<CapSkill.Builder, CapSkill.Reader> skillList = StructList.newFactory(CapSkill.factory);
        static final StructList.Factory<CapBuff.Builder, CapBuff.Reader> buffList = StructList.newFactory(CapBuff.factory);

        static final class Factory extends StructFactory<Builder, Reader> {
            public Builder constructBuilder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) {
                return new Builder(segment, data, pointers, dataSize, pointerCount);
            }
            public Reader constructReader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) {
                return new Reader(segment, data, pointers, dataSize, pointerCount, nestingLimit);
            }
            public StructSize structSize() { return new StructSize((short) 7, (short) 5); }
            public Reader asReader(Builder builder) { return new Reader(); }
        }

        static final class Builder extends StructBuilder {
            Builder(SegmentBuilder segment, int data, int pointers, int dataSize, short pointerCount) { super(segment, data, pointers, dataSize, pointerCount); }
            void setBattleId(long value) { _setLongField(0, value); }
            void setServerTime(long value) { _setLongField(1, value); }
            void setFrameNo(int value) { _setIntField(4, value); }
            void setRoomId(int value) { _setIntField(5, value); }
            void setChecksum(int value) { _setIntField(6, value); }
            void setState(int value) { _setIntField(7, value); }
            void setActorX(float value) { _setFloatField(8, value); }
            void setActorY(float value) { _setFloatField(9, value); }
            void setActorZ(float value) { _setFloatField(10, value); }
            void setReplay(boolean value) { _setBooleanField(352, value); }
            void setRemark(String value) { _setPointerField(Text.factory, 0, new Text.Reader(value)); }
            PrimitiveList.Int.Builder initChangedSlots(int size) { return _initPointerField(PrimitiveList.Int.factory, 1, size); }
            void setPayload(byte[] bytes) { _setPointerField(Data.factory, 2, new Data.Reader(bytes)); }
            StructList.Builder<CapSkill.Builder> initCasts(int size) { return _initPointerField(skillList, 3, size); }
            StructList.Builder<CapBuff.Builder> initBuffs(int size) { return _initPointerField(buffList, 4, size); }
        }

        static final class Reader extends StructReader {
            Reader() { super(); }
            Reader(SegmentReader segment, int data, int pointers, int dataSize, short pointerCount, int nestingLimit) { super(segment, data, pointers, dataSize, pointerCount, nestingLimit); }
            long battleId() { return _getLongField(0); }
            long serverTime() { return _getLongField(1); }
            int frameNo() { return _getIntField(4); }
            int roomId() { return _getIntField(5); }
            int checksum() { return _getIntField(6); }
            int state() { return _getIntField(7); }
            float actorX() { return _getFloatField(8); }
            float actorY() { return _getFloatField(9); }
            float actorZ() { return _getFloatField(10); }
            boolean replay() { return _getBooleanField(352); }
            String remark() { return _getPointerField(Text.factory, 0).toString(); }
            Text.Reader remarkReader() { return _getPointerField(Text.factory, 0); }
            PrimitiveList.Int.Reader changedSlots() { return _getPointerField(PrimitiveList.Int.factory, 1); }
            byte[] payload() { return _getPointerField(Data.factory, 2).toArray(); }
            Data.Reader payloadReader() { return _getPointerField(Data.factory, 2); }
            StructList.Reader<CapSkill.Reader> casts() { return _getPointerField(skillList, 3); }
            StructList.Reader<CapBuff.Reader> buffs() { return _getPointerField(buffList, 4); }
        }
    }
}
