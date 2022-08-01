package com.github.dc.utils;

import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/1 10:07
 */
public abstract class ParallelThreadExecutor {

    public void handle(int nThreads, Consumer<Integer> handler) {
        ExecutorService pool = new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        for (int t = 0; t < nThreads; t++) {
            final int currentThreadNum = t;
            pool.execute(() -> handler.accept(currentThreadNum));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            pool.shutdownNow();
        }
    }
}
