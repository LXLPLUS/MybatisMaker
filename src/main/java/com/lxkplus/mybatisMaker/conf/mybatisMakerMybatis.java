package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;
@Data
@ConfigurationProperties(prefix = "mybatis-maker.mybatis")
@Component
public class mybatisMakerMybatis {
    Set<String> datetimeNotShow;
}
