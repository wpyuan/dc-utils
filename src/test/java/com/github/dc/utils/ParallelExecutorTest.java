package com.github.dc.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author wangpeiyuan
 * @date 2022/8/1 9:52
 */
public class ParallelExecutorTest {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(String.format("测试-[%s]", i));
        }
        parallel(list);
        sync(list);
    }

    public static void parallel(List<String> list) {
        long start = System.currentTimeMillis();
        ParallelExecutor.of(list).run((batchList) -> batchList.forEach(ParallelExecutorTest::otherBusiness));
        System.out.println("并行执行耗时：" + (System.currentTimeMillis() - start) + "毫秒");
    }

    public static void sync(List<String> list) {
        long start = System.currentTimeMillis();
        list.forEach(ParallelExecutorTest::otherBusiness);
        System.out.println("同步执行耗时：" + (System.currentTimeMillis() - start) + "毫秒");
    }

    public static void otherBusiness(String str) {
        // 模拟其他业务逻辑执行耗时10ms
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
