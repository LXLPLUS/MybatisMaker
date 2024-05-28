package com.lxkplus.mybatisMaker.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdom2.Element;
import org.springframework.javapoet.MethodSpec;

@Data
public class Jpa2MybatisBuilder {
    Element xml;
    MethodSpec.Builder mapper;
    boolean mybatis;
    boolean mybatisPlus;
    String table;
    String funcName;
    String alias;


    public Jpa2MybatisBuilder(JpaRow jpaRow) {
        this.setMybatis(jpaRow.isMybatis());
        this.setMybatisPlus(jpaRow.isMybatisPlus());
        this.setTable(jpaRow.getTable());
        this.setFuncName(jpaRow.getFuncName());
        this.setAlias(jpaRow.getAlias());
    }
}
