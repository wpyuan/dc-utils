package com.github.dc.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * ID生成器
 * </p>
 *
 * @author wangpeiyuan
 * @date 2021/7/13 8:59
 */
@UtilityClass
public class IdGenerator {

    /**
     * 64位ID生成器map
     */
    private static final ConcurrentHashMap<String, SnowFlakeImpl> BUILDER_64 = new ConcurrentHashMap<>();

    /**
     * 53位ID生成器map（适配JS安全整数范围）
     */
    private static final ConcurrentHashMap<Integer, SnowFlake53Bit> BUILDER_53 = new ConcurrentHashMap<>();

    /**
     * 获取64位雪花ID（原始版本，可能超出JS安全整数范围）
     *
     * @param dataCenterId 数据中心标识（0-31）
     * @param machineId    机器标识（0-31）
     * @return 64位ID
     */
    public Long get(Integer dataCenterId, Integer machineId) {
        String key = dataCenterId + "_" + machineId;
        SnowFlakeImpl snowFlake = BUILDER_64.computeIfAbsent(key, k -> new SnowFlakeImpl(dataCenterId, machineId));
        return snowFlake.nextId();
    }

    /**
     * 获取53位雪花ID（适配JS安全整数范围，前端可直接使用）
     * <p>
     * 结构：39位时间戳 + 2位机器ID + 12位序列号 = 53位
     * - 有效期：2026-03-03 至 2043-08
     * - 支持机器数：4台（0-3）
     * - 每毫秒并发：4096个
     * </p>
     *
     * @param machineId 机器标识（0-3）
     * @return 53位ID，不超过JS安全整数范围
     */
    public Long getForJs(Integer machineId) {
        SnowFlake53Bit snowFlake = BUILDER_53.computeIfAbsent(machineId, SnowFlake53Bit::new);
        return snowFlake.nextId();
    }

}
