package com.lxkplus.mybatisMaker.service.FileCreateService.Jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lxkplus.mybatisMaker.dto.JpaRow;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JpaLoadService {

    @Getter
    List<JpaRow> jpaRowList = new ArrayList<>();

    ObjectMapper objectMapper;

    @PostConstruct
    void load() throws IOException {

        objectMapper = new ObjectMapper(new YAMLFactory());

        try(InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("jpa.yaml")) {
            if (resourceAsStream != null) {
                jpaRowList = objectMapper.readValue(resourceAsStream.readAllBytes(), new TypeReference<>() {
                });
            }
        }
    }


}
