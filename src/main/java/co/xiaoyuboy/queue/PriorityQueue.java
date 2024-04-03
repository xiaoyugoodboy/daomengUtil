package co.xiaoyuboy.queue;




import java.util.Arrays;

/**
 * @Author: Smile
 * @Date: 2024-03-16 15:45
 * @Description: 优先队列
 */
public class PriorityQueue<E> implements Queue<E>{
//    //日志输出
//    private Logger logger =LoggerFactory.getLogger(PriorityQueue.class);

    //默认初始化容量
    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    //存放优先队列的数组
    transient  Object[] queue;

    //当前队列中元素的个数
    private int size;

    //初始化优先队列(设置成默认容量)
    public PriorityQueue() {
        queue = new Object[DEFAULT_INITIAL_CAPACITY];
    }
    //入队操作
    @Override
    public boolean offer(E e) {
        //检查是否为空
        if (null==e){
            throw  new NullPointerException("插入元素为空值");
        }
        //当前队列的大小.这里的i也是新元素插入的位置，因为数组是从0开始的
        int i = size;
        //判断队列是否已经满了
        if (i>=queue.length){
            //扩容操作grow
            grow(i+1);//i+1是最小需要的容量大小
        }
        //蒋队列的大小增加1
        size=i+1;
        //如果队列原来是空的，就把新元素放置到第一个位置
        if (i==0){
            queue[0]=e;
        }else{
            //如果不是空,就采用上浮操作(i是插入位置,e是元素值)
            siftUp(i,e);
        }
        return true;
    }
    private void siftUp(int k, E x) {
        siftUpComparable(k, x);
    }
    /**
     * 队列扩容
     *
     * @param minCapacity
     */
    private void grow(int minCapacity){
        //获取旧的容量
        int oldCapacity = queue.length;
        //如果当前容量小于64，那么新容量就是当前容量加2，否则新容量就是当前容量的1.5倍
        int newCapacity=oldCapacity+(oldCapacity>64?oldCapacity>>1:oldCapacity+2);
        //确保新容量长度不会超过Integer类型的最大值
        if(newCapacity-(Integer.MAX_VALUE-8)>0){//这里使用Integer.MAX_VALUE-8是为了防止溢出
            //最少需要的容量大于最大类型值了，就直接设置成最大类型值，如果不是就设置成Integer.MAX_VALUE-8
            newCapacity=(minCapacity>Integer.MAX_VALUE-8)?Integer.MAX_VALUE:Integer.MAX_VALUE-8;
        }
        //拷贝数组
        queue= Arrays.copyOf(queue,newCapacity);

    }
    //优先队列上浮操作
    private void siftUpComparable(int k, E x){
        //将传入元素转换成Comparable类型 为了后续可以使用compareTo方法进行比较
        Comparable<E> key = (Comparable<E>)x;
//        logger.info("入队元素是--->{}----当前队列:{}"+ JSON.toJSONString(key), JSON.toJSONString(queue));
        //判断是否是根节点,不是根节点就执行上浮操作
        while (k>0){
            //获取父节点的索引(>>>相当于除2向下取整)
            int parent=(k-1)>>>1;
            //记录日志，当前节点的父节点的位置
//            logger.info("[入队]当前节点的父节点的位置是--->key:{} parent:{}", key,parent);
            //获取到父节点的元素
            Object e=queue[parent];
            //插入的节点和父节点比较，大于父节点就退出循环
            if(key.compareTo((E)e)>=0){
                //日志记录比较情况
//                logger.info("[入队]值对比，父节点:{} 目标节点:{}",JSON.toJSONString(e),JSON.toJSONString(key));
                break;
            }else {
                //日志记录替换过程
//                logger.info("[入队]替换过程，父子节点位置替换，继续循环.父节点的值:{}  存放到位置:{}",JSON.toJSONString(e),JSON.toJSONString(key));

                //替换的逻辑
                queue[k]=e;//把父节点替换下来
                //更新k为父节点的索引，继续向上比较
                k=parent;
            }
        }
        //退出循环之后，k的值就是正确的位置
        queue[k]=key;
        //记录日志表示这次入队操作完成了
//        logger.info("入队操作完成，IdX:{}---Val：{}---当前队列:{}",k,key,JSON.toJSONString(queue));

    }

    //优先队列下沉操作
    private void siftDownComparable(int k, E x){
        //把传入的元素转换成可以比较的类型
        Comparable<E> key = (Comparable<E>)x;
        //计算队列一半的大小，只有非叶子节点需要执行下沉操作
        int half = this.size>>>1;//(使用无符号右移，等价于除以2)
        while (k<half){//小于half就是非叶子节点(执行下沉操作)
            //找到左子节点的索引
            int child=(k<<1)+1;//左子节点再数组中的位置(因为不是叶子节点，左节点一定是存在的)
            //把左子节点的值暂时存储再变量c中
            Object c=queue[child];
            //处理可能存在的右子节点，获取到索引值
            int right=child+1;
            //判断右节点是否存在,并且判断右节点的值是否小于左节点
            if(right<this.size&&((Comparable)c).compareTo((E)queue[right])>0){
                //记录小的那个索引赋值给child 值赋值给c
                c=queue[child=right];
            }
            //比较key(即将下沉的元素) 和当前较小的c
            if (key.compareTo((E) c)<=0){
                //那么当前就是合适的位置
                break;
            }
            //走到这里就是要执行下沉操作
            //讲较小的子节点上移
            queue[k]=c;
            //更新k的值让他继续下沉
            k=child;
        }
        //第k个位置就是key元素的最终位置
        queue[k]=key;
    }
    //添加元素
    @Override
    public boolean add(E e) {
        return offer(e);
    }

    private void siftDown(int k, E x) {
        siftDownComparable(k, x);
    }


    @Override
    public E peek() {
         return (size == 0) ? null : (E) queue[0];
    }

    @Override
    public E poll() {
        //队列没有元素可以移除
        if (size==0){
            return null;
        }
        //最后一个元素的位置
        int s=--size;
        //取出并且保存队列的第一个元素(这是需要返回的优先队列中优先级最高的元素)
        E result= (E) queue[0];
        //保存队列中最后一个元素(这是要重新进行堆调整的元素)
        E x= (E) queue[s];
        queue[s]=null;//help gc
        if(s!=0){
            siftDown(0,x);
        }
        //返回队列中优先级最高的元素
        return result;
    }
    //获取到队列长度
    @Override
    public int size() {
        return this.size;
    }
}
