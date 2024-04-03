package co.xiaoyuboy.queue;

public interface BlockingQueue<E> extends Queue<E>{
    @Override
    boolean add(E e);
    @Override
    boolean offer(E e);

}
