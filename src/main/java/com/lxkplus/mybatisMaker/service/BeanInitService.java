package com.lxkplus.mybatisMaker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class BeanInitService {
    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
