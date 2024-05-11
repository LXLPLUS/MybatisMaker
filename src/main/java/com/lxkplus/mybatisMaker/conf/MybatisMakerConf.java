package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "mybatis-maker")
@Component
public class MybatisMakerConf {
    private Map<String, String> typeMapper = new HashMap<>();
    private List<String> trimFirst;
    private List<String> trimLast;
    private List<String> lombok;
    private boolean swagger3Exist;
    private boolean javaDocExist;
    private boolean useLombok;
    private boolean clearHistory;
}
