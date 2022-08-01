package com.github.dc.utils;

import com.github.dc.utils.pojo.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <p>
 *     并行执行器
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/1 9:10
 */
public class ParallelExecutor<E> extends ParallelThreadExecutor {

    /**
     * 需要处理的list数据
     */
    private List<E> list;

    /**
     * 执行逻辑
     * @param custom 客户化逻辑。参数是分片数据
     */
    public void run(Consumer<List<E>> custom) {
        // 线程数，阻塞系数为0.9
        int threadCount = Runtime.getRuntime().availableProcessors() * 10;
        threadCount = Math.min(threadCount, this.list.size());
        // 均摊到每个线程执行的条数，
        int[] threadLength = new int[threadCount];
        int allLength = this.list.size();
        int presetLength = allLength / threadCount;
        int remainingLength = allLength % threadCount;
        Map<Integer, Pair<Integer, Integer>> indexMap = new HashMap<>(threadLength.length);
        int startIndex = 0;
        for (int i = 0; i < threadLength.length; i++) {
            if (remainingLength > 0) {
                threadLength[i] = presetLength + 1;
                remainingLength --;
            } else {
                threadLength[i] = presetLength;
            }
            indexMap.put(i, Pair.of(startIndex, startIndex + threadLength[i]));
            startIndex = startIndex + threadLength[i];
        }

        this.handle(threadCount, (currentThreadNum) -> {
            List<E> batchList = this.list.subList(indexMap.get(currentThreadNum).getFirst(), indexMap.get(currentThreadNum).getSecond());
            custom.accept(batchList);
        });

    }

    public static <E> ParallelExecutor<E> of(List<E> list) {
        ParallelExecutor<E> parallelExecutor = new ParallelExecutor<E>();
        parallelExecutor.list = list;
        return parallelExecutor;
    }
}
