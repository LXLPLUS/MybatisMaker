package com.lxkplus.mybatisMaker.manager;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class CountManager {
    private int count;

    private long changedStamp = System.currentTimeMillis();

    private long lastCheckStamp = System.currentTimeMillis();

    public void addCount() {
        count++;
        MDC.put("count", Integer.toString(count));
    }

    public void change() {
        changedStamp = System.currentTimeMillis();
    }

    public void check() {
        lastCheckStamp = System.currentTimeMillis();
    }

    public long timeFromLastChange() {
        return System.currentTimeMillis() - changedStamp;
    }

    public boolean timeLongerThanLastCheck(long timeMillis) {
        long stamp = System.currentTimeMillis();
        return stamp - lastCheckStamp >= timeMillis;
    }
}
