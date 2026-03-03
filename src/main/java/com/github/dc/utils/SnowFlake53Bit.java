package com.github.dc.utils;

/**
 * <p>
 *     SnowFlake算法生成ID（53位版本，适配JS安全整数范围）
 *     结构：39位时间戳 + 2位机器ID + 12位序列号 = 53位
 *     - 时间戳39位：约17年有效期（2026-03-03 至 2043-08）
 *     - 机器ID 2位：支持4台机器（0-3）
 *     - 序列号12位：每毫秒4096个ID
 * </p>
 * <p>
 *     原始Twitter算法参考：https://github.com/beyondfengyu/SnowFlake
 * </p>
 *
 * @author wangpeiyuan
 * @date 2026/03/03
 */
public class SnowFlake53Bit {

    /**
     * 起始时间戳：2026-03-03 00:00:00 UTC
     * 39位时间戳可用约17年，即到2043年8月
     */
    private final static long START_TIMESTAMP = 1772496000000L;

    /**
     * 每一部分占用的位数（总计53位，适配JS安全整数）
     */
    // 序列号占用的位数：12位，每毫秒可生成4096个ID
    private final static long SEQUENCE_BIT = 12;
    // 机器标识占用的位数：2位，支持0-3共4台机器
    private final static long MACHINE_BIT = 2;

    /**
     * 每一部分的最大值
     */
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    // 机器标识
    private final long machineId;
    // 序列号
    private long sequence = 0L;
    // 上一次时间戳
    private long lastTimeStamp = -1L;

    private long getNextMill() {
        long mill = getNewTimeStamp();
        while (mill <= lastTimeStamp) {
            mill = getNewTimeStamp();
        }
        return mill;
    }

    private long getNewTimeStamp() {
        return System.currentTimeMillis();
    }

    /**
     * 根据机器标志ID生成ID生成器
     *
     * @param machineId 机器标志ID（0-3）
     */
    public SnowFlake53Bit(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("MachineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0！");
        }
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return id（53位，不超过JS安全整数范围）
     */
    public synchronized long nextId() {
        long currTimeStamp = getNewTimeStamp();
        if (currTimeStamp < lastTimeStamp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currTimeStamp == lastTimeStamp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currTimeStamp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastTimeStamp = currTimeStamp;
        // 时间戳部分 + 机器标识部分 + 序列号部分 = 53位
        return ((currTimeStamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }
}
