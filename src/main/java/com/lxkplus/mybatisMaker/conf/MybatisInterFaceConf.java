package com.lxkplus.mybatisMaker.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "mybatis-maker.mybatis.sql")
@Component
public class MybatisInterFaceConf {
    boolean insert;
    boolean deleteById;
    boolean updateById;
    boolean selectById;
    boolean selectByIds;
    boolean deleteByIds;
    boolean insertList;
}
