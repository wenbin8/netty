package com.wenbin.bio2.thread.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: wenbin
 * @Date: 2019/6/12 06:21
 * @Description:
 */
public class TimeServerHandlerExecutePool {

    private ExecutorService executorService;

    public TimeServerHandlerExecutePool(int maxPoolSize, int queueSizee) {
        executorService = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors()
                , maxPoolSize, 120L, TimeUnit.SECONDS
                , new ArrayBlockingQueue<Runnable>(queueSizee));
    }

    public void execute(Runnable task) {
        executorService.execute(task);
    }
}
