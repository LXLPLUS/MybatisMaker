package com.lxkplus.mybatisMaker.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.enums.Package;
import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableFlowContext {
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
    Package mybatisMapperPackage;

    /**
     * mybatis-plus package
     */
    Package mybatisPlusPackage;
    Package mybatisPlusMapperPackage;
    Package mybatisPlusXmlPackage;
    /**
     * mybatis path
     * 文件地址
     */
    Path mybatisXmlPath;
    Path mybatisEntityPath;
    Path MybatisMapperPath;

    /**
     * mybatis-plus path
     * 文件地址
     */
    Path mybatisPlusXmlPath;
    Path mybatisPlusEntityPath;
    Path MybatisPlusMapperPath;

    String mapperName;
    /**
     * xml 限定名
     */
    String mybatisResultMapId;

    boolean activeDatabase;
    String safeTableName;

    List<Jpa2MybatisBuilder> jpa2MybatisBuilders = new ArrayList<>();


    public static TableFlowContext fromColumn(Column column) {
        TableFlowContext tableFlowContext = new TableFlowContext();
        tableFlowContext.setTableCatalog(column.getTableCatalog());
        tableFlowContext.setTableSchema(column.getTableSchema());
        tableFlowContext.setTableName(column.getTableName());
        return tableFlowContext;
    }
}
