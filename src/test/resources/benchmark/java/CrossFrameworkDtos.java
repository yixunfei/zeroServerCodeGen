package com.zero.codegen.benchmark.generated;

import java.util.ArrayList;
import java.util.List;

public final class CrossFrameworkDtos {
    private CrossFrameworkDtos() {
    }

    public enum State {
        IDLE,
        MATCHING,
        FIGHTING,
        SETTLING
    }

    public static final class Skill {
        public int skillId;
        public int damage;
        public boolean critical;
        public float ratio;
    }

    public static final class Buff {
        public int slot;
        public int buffId;
        public int level;
        public long expireAt;
        public boolean positive;
    }

    public static final class Frame {
        public long battleId;
        public long serverTime;
        public int frameNo;
        public int roomId;
        public int checksum;
        public float actorX;
        public float actorY;
        public float actorZ;
        public State state;
        public boolean replay;
        public String remark;
        public int[] changedSlots = new int[0];
        public byte[] payload = new byte[0];
        public List<Skill> casts = new ArrayList<>();
        public List<Buff> buffs = new ArrayList<>();
    }

    public static Frame fromGenerated(CrossFrameworkFrame source) {
        Frame frame = new Frame();
        frame.battleId = source.getBattleId();
        frame.serverTime = source.getServerTime();
        frame.frameNo = source.getFrameNo();
        frame.roomId = source.getRoomId();
        frame.checksum = source.getChecksum();
        frame.actorX = source.getActorX();
        frame.actorY = source.getActorY();
        frame.actorZ = source.getActorZ();
        frame.state = State.valueOf(source.getState().name());
        frame.replay = source.getReplay();
        frame.remark = source.getRemark();
        frame.changedSlots = source.getChangedSlots();
        frame.payload = source.getPayload();
        for (CfSkill skill : source.getCasts()) {
            Skill dto = new Skill();
            dto.skillId = skill.getSkillId();
            dto.damage = skill.getDamage();
            dto.critical = skill.getCritical();
            dto.ratio = skill.getRatio();
            frame.casts.add(dto);
        }
        for (CfBuff buff : source.getBuffs()) {
            Buff dto = new Buff();
            dto.slot = buff.getSlot();
            dto.buffId = buff.getBuffId();
            dto.level = buff.getLevel();
            dto.expireAt = buff.getExpireAt();
            dto.positive = buff.getPositive();
            frame.buffs.add(dto);
        }
        return frame;
    }

    public static long blackHole(Frame frame) {
        long value = frame.battleId ^ frame.serverTime ^ frame.frameNo ^ frame.roomId ^ frame.checksum;
        value += frame.changedSlots.length + frame.payload.length + frame.casts.size() + frame.buffs.size();
        value += frame.state == null ? 0 : frame.state.ordinal();
        value += frame.replay ? 17 : 3;
        value += frame.remark == null ? 0 : frame.remark.length();
        if (!frame.casts.isEmpty()) {
            value += frame.casts.get(0).damage;
        }
        if (!frame.buffs.isEmpty()) {
            value += frame.buffs.get(0).expireAt;
        }
        return value;
    }
}
