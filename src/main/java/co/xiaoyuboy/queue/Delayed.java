package co.xiaoyuboy.queue;

import java.util.concurrent.TimeUnit;

/**
 * 延迟队列接口
 */
public interface Delayed extends Comparable<Delayed> {
    long getDelay(TimeUnit unit);
}
