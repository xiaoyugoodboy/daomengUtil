package co.xiaoyuboy.entity;

import co.xiaoyuboy.queue.Delayed;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Smile
 * @Date: 2024-03-21 15:12
 * @Description:
 */
public class Job implements Delayed {
    //请求体
    private String body;
    //开始时间
    private Long begin;
    //延迟时间
    private Long delayTime;
    //原文
    private String  jsonBody;

    public Job() {
    }

    public Job(String body, Long begin, Long delayTime) {
        this.body = body;
        this.begin = begin;
        this.delayTime = delayTime;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Long delayTime) {
        this.delayTime = delayTime;
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public void setJsonBody(String jsonBody) {
        this.jsonBody = jsonBody;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(begin + delayTime - System.currentTimeMillis(), TimeUnit.MICROSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        Job job = (Job) o;
        return (int) (this.getDelay(TimeUnit.MICROSECONDS) - job.getDelay(TimeUnit.MICROSECONDS));
    }

    @Override
    public String toString() {
        return "Job{" +
                "body='" + body + '\'' +
                ", begin=" + begin +
                ", delayTime=" + delayTime +
                ", jsonBody='" + jsonBody + '\'' +
                '}';
    }
}
