package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "mybatis-maker.generate")
@Component
public class GenerateConf {
    boolean mybatis;
    boolean mybatisPlus;
}
