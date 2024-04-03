package co.xiaoyuboy.queue;

/**
 * 双端队列
 * @param <E>
 */

public interface Deque<E> extends Queue<E> {
    //给头部插入
    boolean addFirst(E e);
    //给尾部插入
    boolean addLast(E e);
}
