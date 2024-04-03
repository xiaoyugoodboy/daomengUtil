package co.xiaoyuboy.queue;

/**
 * 单端队列
 */
public interface Queue<E> {

    boolean add(E e);
    boolean offer(E e);

    /**
     * 从队列头部移除一个元素，并返回该元素。如果队列为空则返回null。
     * @return
     */
    E peek();

    /**
     *返回队列头部的元素，但不移除它。如果队列为空则返回null。
     * @return
     */
    E poll();
    // 获取队列长度
    int size();

}
