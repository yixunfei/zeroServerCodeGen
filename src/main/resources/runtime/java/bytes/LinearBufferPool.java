package com.zero.codegen.runtime.bytes;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 独立于 Netty 的轻量线性 buffer 池。
 *
 * 这里的目标不是做一个复杂的通用对象池，而是专门服务协议 payload 的高频顺序写入场景：
 * 1. 优先复用线程内 buffer，减少锁竞争。
 * 2. 线程内没有可用对象时，再从一个很小的全局池兜底。
 * 3. 回收超大 buffer 时主动降容，避免偶发大包把池长期“撑胖”。
 *
 * 设计取舍：
 * - 简单、可审查优先。
 * - 只处理 acquire/release，不把生命周期逻辑扩散到业务代码。
 * - 不复用 Netty 自身线程池或 ByteBuf 池，降低耦合与排障复杂度。
 */
public final class LinearBufferPool {
    private static final int DEFAULT_CAPACITY = 1024;
    private static final int MAX_RETAINED_CAPACITY = 64 * 1024;
    private static final int MAX_GLOBAL_POOL_SIZE = 128;

    private static final ThreadLocal<LinearByteBuffer> LOCAL = new ThreadLocal<>();
    private static final Deque<LinearByteBuffer> GLOBAL = new ArrayDeque<>();

    private LinearBufferPool(){}

    public static LinearByteBuffer acquire(){
        return acquire(DEFAULT_CAPACITY);
    }

    public static LinearByteBuffer acquire(int expectedCapacity){
        int initialCapacity=Math.max(DEFAULT_CAPACITY, expectedCapacity);
        LinearByteBuffer local=LOCAL.get();
        if(local!=null){
            LOCAL.remove();
            local.clear();
            local.ensureWritable(initialCapacity);
            return local;
        }
        synchronized (GLOBAL){
            LinearByteBuffer pooled=GLOBAL.pollFirst();
            if(pooled!=null){
                pooled.clear();
                pooled.ensureWritable(initialCapacity);
                return pooled;
            }
        }
        return new LinearByteBuffer(initialCapacity);
    }

    /**
     * 归还时只做两件事：
     * 1. 清空读写索引并在必要时缩容。
     * 2. 尽量回收到线程内，其次回收到一个小的全局池。
     *
     * 这样做可以把复用命中率和实现复杂度控制在一个比较平衡的点。
     */
    public static void release(LinearByteBuffer buffer){
        if(buffer==null){
            return;
        }
        buffer.resetForPool(DEFAULT_CAPACITY, MAX_RETAINED_CAPACITY);
        if(LOCAL.get()==null){
            LOCAL.set(buffer);
            return;
        }
        synchronized (GLOBAL){
            if(GLOBAL.size()<MAX_GLOBAL_POOL_SIZE){
                GLOBAL.addFirst(buffer);
            }
        }
    }
}

