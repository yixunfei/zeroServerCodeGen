package com.zero.codegen.benchmark.generated;

import com.google.protobuf.ByteString;
import com.zero.codegen.benchmark.proto.BenchmarkCrossFrameProto;

public final class GeneratedProtobufCodecs {
    private GeneratedProtobufCodecs() {
    }

    public static byte[] encode(CrossFrameworkDtos.Frame frame) {
        BenchmarkCrossFrameProto.CrossFrameworkFrameProto.Builder builder =
                BenchmarkCrossFrameProto.CrossFrameworkFrameProto.newBuilder()
                        .setBattleId(frame.battleId)
                        .setServerTime(frame.serverTime)
                        .setFrameNo(frame.frameNo)
                        .setRoomId(frame.roomId)
                        .setChecksum(frame.checksum)
                        .setActorX(frame.actorX)
                        .setActorY(frame.actorY)
                        .setActorZ(frame.actorZ)
                        .setState(toProtoState(frame.state))
                        .setReplay(frame.replay)
                        .setRemark(frame.remark)
                        .setPayload(ByteString.copyFrom(frame.payload));
        for (int slot : frame.changedSlots) {
            builder.addChangedSlots(slot);
        }
        for (CrossFrameworkDtos.Skill source : frame.casts) {
            builder.addCasts(BenchmarkCrossFrameProto.CfSkillProto.newBuilder()
                    .setSkillId(source.skillId)
                    .setDamage(source.damage)
                    .setCritical(source.critical)
                    .setRatio(source.ratio));
        }
        for (CrossFrameworkDtos.Buff source : frame.buffs) {
            builder.addBuffs(BenchmarkCrossFrameProto.CfBuffProto.newBuilder()
                    .setSlot(source.slot)
                    .setBuffId(source.buffId)
                    .setLevel(source.level)
                    .setExpireAt(source.expireAt)
                    .setPositive(source.positive));
        }
        return builder.build().toByteArray();
    }

    public static CrossFrameworkDtos.Frame decode(byte[] bytes) throws Exception {
        BenchmarkCrossFrameProto.CrossFrameworkFrameProto message =
                BenchmarkCrossFrameProto.CrossFrameworkFrameProto.parseFrom(bytes);
        CrossFrameworkDtos.Frame frame = new CrossFrameworkDtos.Frame();
        frame.battleId = message.getBattleId();
        frame.serverTime = message.getServerTime();
        frame.frameNo = message.getFrameNo();
        frame.roomId = message.getRoomId();
        frame.checksum = message.getChecksum();
        frame.actorX = message.getActorX();
        frame.actorY = message.getActorY();
        frame.actorZ = message.getActorZ();
        frame.state = fromProtoState(message.getState());
        frame.replay = message.getReplay();
        frame.remark = message.getRemark();
        frame.changedSlots = new int[message.getChangedSlotsCount()];
        for (int i = 0; i < frame.changedSlots.length; i++) {
            frame.changedSlots[i] = message.getChangedSlots(i);
        }
        frame.payload = message.getPayload().toByteArray();
        for (BenchmarkCrossFrameProto.CfSkillProto source : message.getCastsList()) {
            CrossFrameworkDtos.Skill skill = new CrossFrameworkDtos.Skill();
            skill.skillId = source.getSkillId();
            skill.damage = source.getDamage();
            skill.critical = source.getCritical();
            skill.ratio = source.getRatio();
            frame.casts.add(skill);
        }
        for (BenchmarkCrossFrameProto.CfBuffProto source : message.getBuffsList()) {
            CrossFrameworkDtos.Buff buff = new CrossFrameworkDtos.Buff();
            buff.slot = source.getSlot();
            buff.buffId = source.getBuffId();
            buff.level = source.getLevel();
            buff.expireAt = source.getExpireAt();
            buff.positive = source.getPositive();
            frame.buffs.add(buff);
        }
        return frame;
    }

    public static long randomRead(byte[] bytes) throws Exception {
        BenchmarkCrossFrameProto.CrossFrameworkFrameProto message =
                BenchmarkCrossFrameProto.CrossFrameworkFrameProto.parseFrom(bytes);
        long value = message.getBattleId() ^ message.getServerTime();
        value += message.getChecksum();
        value += message.getRemark().length();
        value += message.getPayload().isEmpty() ? 0 : message.getPayload().byteAt(message.getPayload().size() / 2);
        int slots = message.getChangedSlotsCount();
        if (slots > 0) {
            value += message.getChangedSlots(slots / 2);
        }
        int casts = message.getCastsCount();
        if (casts > 0) {
            BenchmarkCrossFrameProto.CfSkillProto skill = message.getCasts(casts / 2);
            value += skill.getSkillId() + skill.getDamage() + Float.floatToIntBits(skill.getRatio());
        }
        int buffs = message.getBuffsCount();
        if (buffs > 0) {
            BenchmarkCrossFrameProto.CfBuffProto buff = message.getBuffs(buffs / 2);
            value += buff.getBuffId() + buff.getLevel() + buff.getExpireAt();
        }
        value += message.getStateValue() + (message.getReplay() ? 17 : 3);
        return value;
    }

    private static BenchmarkCrossFrameProto.CfStateProto toProtoState(CrossFrameworkDtos.State state) {
        return switch (state) {
            case MATCHING -> BenchmarkCrossFrameProto.CfStateProto.MATCHING;
            case FIGHTING -> BenchmarkCrossFrameProto.CfStateProto.FIGHTING;
            case SETTLING -> BenchmarkCrossFrameProto.CfStateProto.SETTLING;
            default -> BenchmarkCrossFrameProto.CfStateProto.IDLE;
        };
    }

    private static CrossFrameworkDtos.State fromProtoState(BenchmarkCrossFrameProto.CfStateProto state) {
        return switch (state) {
            case MATCHING -> CrossFrameworkDtos.State.MATCHING;
            case FIGHTING -> CrossFrameworkDtos.State.FIGHTING;
            case SETTLING -> CrossFrameworkDtos.State.SETTLING;
            default -> CrossFrameworkDtos.State.IDLE;
        };
    }
}
