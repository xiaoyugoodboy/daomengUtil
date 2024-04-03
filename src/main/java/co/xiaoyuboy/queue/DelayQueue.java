package co.xiaoyuboy.queue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @Author: Smile
 * @Date: 2024-03-17 19:38
 * @Description: 延迟队列
 */
public class DelayQueue<E extends Delayed> implements BlockingQueue<E> {

    private final transient ReentrantLock lock = new ReentrantLock(); // 定义一个可重入锁，用于控制对队列的并发访问
    private final PriorityQueue<E> q = new PriorityQueue<>(); // 定义一个优先队列，用于存储队列元素

    private final Condition available = lock.newCondition(); // 使用lock创建一个Condition实例，用于线程间的协调通信


    @Override
    public boolean add(E e) {// 实现BlockingQueue接口的add方法
        return offer(e); // 直接调用offer方法来添加元素，这里假设offer始终返回true
    }

    @Override
    public E peek() {
        final ReentrantLock lock=this.lock;//获取当前lock对象的引用
        lock.lock();//加锁
        try{
            return q.peek();//返回头部元素但是不移除
        }finally {
            lock.unlock();//释放锁
        }
    }

    @Override
    public E poll() {//实现BlockingQueue接口的poll方法
        final ReentrantLock lock=this.lock;//获取对当前lock对象的引用
        lock.lock();//加锁
        try{
            E first=q.peek();//查看优先队列的头部元素，但是不移除
            if (null==first||first.getDelay(NANOSECONDS)>0){//如果优先队列为空或者优先队列的头部元素的延时时间大于0
                return null;//返回null,表示没有元素可以移除
            }else{
                return q.poll();//移除并且返回头部元素
            }
        }finally {
            lock.unlock();//释放锁
        }
    }

    @Override
    public int size() {
        final ReentrantLock lock=this.lock;//获取当前lock对象的引用
        lock.lock();//加锁
        try{
            return q.size();//获取到队列长度
        }finally {
            lock.unlock();//释放锁
        }
    }

    @Override
    public boolean offer(E e) {//实现延迟队列接口中的offer方法
        final ReentrantLock lock=this.lock;//获取对当前lock对象的引用
        lock.lock();//加锁
        try {
            q.offer(e);//把元素e加入到优先队列中
            if (q.peek()==e){//如果e现在是队列的头部元素
                available.signal();//唤醒队列中等待的线程
            }
            return true;//调价元素成功
        }finally {
            lock.unlock();//释放锁
        }
    }
}


