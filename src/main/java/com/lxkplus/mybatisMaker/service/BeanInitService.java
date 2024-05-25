package com.lxkplus.mybatisMaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
public class BeanInitService {

    final ObjectMapper jsonObjectMapper;

    final ObjectMapper yamlObjectMapper;

    public BeanInitService() {
        jsonObjectMapper = new ObjectMapper();
        yamlObjectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    }

    @Primary
    @Bean(name = "jsonObjectMapper")
    public ObjectMapper getJsonObjectMapper() {
        return jsonObjectMapper;
    }

    @Bean(name = "yamlObjectMapper")
    public ObjectMapper getYamlObjectMapper() {
        return yamlObjectMapper;
    }

}
