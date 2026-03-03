package com.github.dc.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * ID生成器（53位版本，适配JS安全整数范围）
 * </p>
 *
 * @author wangpeiyuan
 * @date 2021/7/13 8:59
 */
@UtilityClass
public class IdGenerator {

    /**
     * id生成器map
     */
    public static final ConcurrentHashMap<Integer, SnowFlakeImpl> BUILDER = new ConcurrentHashMap<>();

    /**
     * 获取单个id（53位，不超过JS安全整数范围）
     *
     * @param machineId 机器标识（0-3）
     * @return id
     */
    public Long get(Integer machineId) {
        SnowFlakeImpl snowFlake = BUILDER.computeIfAbsent(machineId, SnowFlakeImpl::new);
        return snowFlake.nextId();
    }

    /**
     * 兼容旧方法（dataCenterId将被忽略）
     *
     * @param dataCenterId 数据中心标识（已废弃，将被忽略）
     * @param machineId    机器标识（0-3）
     * @return id
     * @deprecated 请使用 {@link #get(Integer)} 方法
     */
    @Deprecated
    public Long get(Integer dataCenterId, Integer machineId) {
        return get(machineId);
    }

}
