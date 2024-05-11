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
    /**
     * 侦听列
     */
    List<ColumnWithJavaStatus> Columns = new ArrayList<>();
    List<ColumnWithJavaStatus> dateTimeAutoColumns = new ArrayList<>();
    ColumnWithJavaStatus idColumn = null;
    /**
     * 主键类型
     */
    IdType idType;
    /**
     * 表格注释
     */
    String tableComment;
    /**
     * 使用引擎
     */
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
     * 文件地址
     */
    Path mybatisXMLPath;
    Path mybatisEntityPath;
    Path mybatisPlusEntityPath;
    Path MybatisMapperPath;
    String mapperName;
    /**
     * xml 限定名
     */
    String mybatisResultMapId;

    boolean activeDatabase;
    String databaseWithTableName;
}
