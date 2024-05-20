package com.lxkplus.mybatisMaker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.entity.Column;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersistenceService {
    @Resource
    MybatisMakerConf mybatisMakerConf;

    @Resource
    ObjectMapper objectMapper;

    Path cachePath;
    @PostConstruct
    void init() {
        String codeRoot = mybatisMakerConf.getCodeRoot();
        cachePath = Path.of(codeRoot, "cache.json");
    }

    @Async
    public void save(List<Column> columns) throws IOException {

        Files.writeString(cachePath, objectMapper.writeValueAsString(columns),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public List<Column> load() throws IOException {
        if (cachePath.toFile().isFile()) {
            String s = Files.readString(cachePath);
            return objectMapper.readValue(s, new TypeReference<>() {});
        }
        return new ArrayList<>();
    }

    public void clearCache() throws IOException {
        Files.deleteIfExists(cachePath);
    }
}
