package com.lxkplus.mybatisMaker.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lxkplus.mybatisMaker.entity.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class CacheManager {
    ConcurrentMap<Long, List<Column>> map;

    public CacheManager() {
        Cache<Long, List<Column>> cache = Caffeine.newBuilder()
                .maximumSize(3)
                .build();
        map = cache.asMap();
    }

    public List<Column> get() {
        Long reduce = map.keySet().stream().reduce(0L, Long::max);
        return map.getOrDefault(reduce, new ArrayList<>());
    }

    public void put(List<Column> columns) {
        map.put(System.currentTimeMillis(), columns);
    }
}