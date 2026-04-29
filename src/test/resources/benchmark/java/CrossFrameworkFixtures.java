package com.zero.codegen.benchmark.generated;

import com.zero.codegen.benchmark.generated.runtime.bytes.ArrayByteCursor;
import com.zero.codegen.benchmark.generated.runtime.proto.PayloadBuilder;
import com.zero.codegen.benchmark.generated.runtime.serialize.ICursorProto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public final class CrossFrameworkFixtures {
    private CrossFrameworkFixtures() {
    }

    public static CrossFrameworkFrame frame(int casts, int buffs, int slots, int payloadBytes) {
        CrossFrameworkFrame frame = new CrossFrameworkFrame();
        frame.setBattleId(700000000000L + casts * 31L + buffs);
        frame.setServerTime(1700000000000L + payloadBytes);
        frame.setFrameNo(4096 + casts);
        frame.setRoomId(300 + buffs);
        frame.setChecksum(casts * 1009 + buffs * 917 + slots * 37 + payloadBytes);
        frame.setActorX(120.25F + casts);
        frame.setActorY(33.5F + buffs);
        frame.setActorZ(-9.75F + slots);
        frame.setState(CfState.FIGHTING);
        frame.setReplay((casts & 1) == 0);
        frame.setRemark("cross-framework-frame-" + casts + "-" + buffs + "-" + payloadBytes);
        int[] changedSlots = new int[slots];
        for (int i = 0; i < changedSlots.length; i++) {
            changedSlots[i] = i * 3 + 1;
        }
        frame.setChangedSlots(changedSlots);
        byte[] payload = new byte[payloadBytes];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i * 13 + 7);
        }
        frame.setPayload(payload);
        List<CfSkill> castList = new ArrayList<>(casts);
        for (int i = 0; i < casts; i++) {
            CfSkill skill = new CfSkill();
            skill.setSkillId(1000 + i);
            skill.setDamage(5000 + i * 17);
            skill.setCritical((i & 3) == 0);
            skill.setRatio(0.5F + i / 100.0F);
            castList.add(skill);
        }
        frame.setCasts(castList);
        List<CfBuff> buffList = new ArrayList<>(buffs);
        for (int i = 0; i < buffs; i++) {
            CfBuff buff = new CfBuff();
            buff.setSlot(i);
            buff.setBuffId(2000 + i);
            buff.setLevel(1 + (i % 9));
            buff.setExpireAt(1700000000000L + i * 1000L);
            buff.setPositive((i & 1) == 0);
            buffList.add(buff);
        }
        frame.setBuffs(buffList);
        return frame;
    }

    public static byte[] encodeCursor(ICursorProto payload) {
        return PayloadBuilder.build(payload::writeTo);
    }

    public static byte[] encodeByteBuf(ICursorProto payload) {
        ByteBuf buf = Unpooled.buffer(Math.max(256, payload.estimatedSize()));
        try {
            payload.writeTo(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        } finally {
            buf.release();
        }
    }

    public static CrossFrameworkFrame decodeCursor(byte[] bytes) {
        return CrossFrameworkFrame.readFrom(new ArrayByteCursor(bytes));
    }

    public static CrossFrameworkFrame decodeByteBuf(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            return CrossFrameworkFrame.readFrom(buffer);
        } finally {
            buffer.release();
        }
    }

    public static void readIntoCursor(byte[] bytes, CrossFrameworkFrame target) {
        CrossFrameworkFrame.readInto(new ArrayByteCursor(bytes), target);
    }

    public static long blackHole(CrossFrameworkFrame frame) {
        long value = frame.getBattleId() ^ frame.getServerTime() ^ frame.getFrameNo();
        value += frame.getChangedSlots().length + frame.getPayload().length + frame.getCasts().size() + frame.getBuffs().size();
        value += frame.getState().ordinal() + (frame.getReplay() ? 17 : 3);
        value += frame.getRemark().length();
        if (!frame.getCasts().isEmpty()) {
            value += frame.getCasts().get(0).getDamage();
        }
        if (!frame.getBuffs().isEmpty()) {
            value += frame.getBuffs().get(0).getExpireAt();
        }
        return value;
    }
}
