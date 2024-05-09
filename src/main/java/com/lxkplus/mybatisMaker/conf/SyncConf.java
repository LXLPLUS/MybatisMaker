package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "mybatis-maker.sync")
@Component
public class SyncConf {
    private List<String> syncTable;
    private List<String> exceptTable;
}
