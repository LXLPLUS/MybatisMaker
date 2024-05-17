package com.lxkplus.mybatisMaker.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lxkplus.mybatisMaker.entity.Column;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class CacheService {
    ConcurrentMap<Long, List<Column>> map = new ConcurrentHashMap<>();
    @PostConstruct
    public void init() {
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
        long l = System.currentTimeMillis();
        map.put(l, columns);
    }
}