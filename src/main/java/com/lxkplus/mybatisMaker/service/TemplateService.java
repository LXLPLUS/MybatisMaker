package com.lxkplus.mybatisMaker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TemplateService {

    @Resource(name = "jsonObjectMapper")
    ObjectMapper objectMapper;

    public String replace(String str, Object bean) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        Map<String, String> map = objectMapper.convertValue(bean, new TypeReference<>() {});
        map.entrySet().forEach(x -> x.setValue(StringUtils.defaultIfBlank(x.getValue(), "")));
        StringSubstitutor stringSubstitutor = new StringSubstitutor(map, "{", "}");
        String replace = stringSubstitutor.replace(str);
        return StringUtils.strip(replace);
    }
}
