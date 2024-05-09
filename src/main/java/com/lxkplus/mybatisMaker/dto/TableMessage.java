package com.lxkplus.mybatisMaker.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.lxkplus.mybatisMaker.enums.Package;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableMessage {
    String tableCatalog;
    String tableSchema;
    String javaBeanName;
    String tableName;
    String tableType;
    String DDL;
    List<ColumnWithJavaStatus> Columns = new ArrayList<>();
    ColumnWithJavaStatus idColumn = null;
    IdType idType;
    String tableComment;
    String engine;
    /**
     * 全限定名
     */
    String fullyQualifiedName;

    Package DDLPackage;
    Path DDLPath;
    /**
     * mybatis package
     */
    Package mybatisXmlPackage;
    Package mybatisBeanPackage;
    Package mybatisPackage;
    Package MybatisMapperPackage;

    /**
     * mybatis-plus package
     */
    Package mybatisPlusPackage;
    /**
     * mybatis path
     */
    Path mybatisXMLPath;
    Path mybatisBeanPath;
    Path mybatisPath;
    Path mybatisPlusPath;
    Path MybatisMapperPath;
    String mapperName;
    /**
     * xml 限定名
     */
    String mybatisResultMapId;

    boolean activeDatabase;
    String databaseWithTableName;
}
