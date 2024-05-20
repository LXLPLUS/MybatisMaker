package com.lxkplus.mybatisMaker.dto;

import lombok.Data;
import org.jdom2.Element;
import org.springframework.javapoet.MethodSpec;

@Data
public class Jpa2MybatisBuilder {
    Element xml;
    MethodSpec.Builder mapper;
}
