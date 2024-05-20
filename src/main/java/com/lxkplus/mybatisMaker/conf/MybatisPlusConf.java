package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "mybatis-maker.mybatis-plus")
@Component
public class MybatisPlusConf {
    String mybatisPlusBeanPackage;
    String mapperPackage;
    String xmlPackage;
}
