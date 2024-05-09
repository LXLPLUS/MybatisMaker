package com.lxkplus.mybatisMaker.dto;

import com.lxkplus.mybatisMaker.po.Column;
import lombok.Data;

import java.lang.reflect.Type;

@Data
public class ColumnWithJavaStatus {
    String tableCatalog;
    String tableSchema;
    String tableName;
    String columnName;
    Integer ordinalPosition;
    String columnDefault;
    Boolean isNullable;
    String dataType;
    Long characterMaximumLength;
    Long characterOctetLength;
    Long numericPrecision;
    String numericScale;
    String datetimePrecision;
    String characterSetName;
    String collationName;
    String columnType;
    String columnKey;
    String extra;
    String privileges;
    String columnComment;
    String generationExpression;
    String srsId;
    String javaColumnName;
    String idType;
    String jdbcType;
    boolean convertMysql2JavaStatus;
    Type javaType;
    /**
     * 是否是时间字段
     */
    boolean checkDateTime;

    // 是否是主键
    boolean pri;

    public static ColumnWithJavaStatus convert(Column column) {
        ColumnWithJavaStatus target = new ColumnWithJavaStatus();
        target.setTableCatalog(column.getTableCatalog());
        target.setTableSchema(column.getTableSchema());
        target.setTableName(column.getTableName());
        target.setColumnName(column.getColumnName());
        target.setOrdinalPosition(column.getOrdinalPosition());
        target.setColumnDefault(column.getColumnDefault());
        target.setIsNullable(column.getIsNullable());
        target.setDataType(column.getDataType());
        target.setCharacterMaximumLength(column.getCharacterMaximumLength());
        target.setCharacterOctetLength(column.getCharacterOctetLength());
        target.setNumericPrecision(column.getNumericPrecision());
        target.setNumericScale(column.getNumericScale());
        target.setDatetimePrecision(column.getDatetimePrecision());
        target.setCharacterSetName(column.getCharacterSetName());
        target.setCollationName(column.getCollationName());
        target.setColumnType(column.getColumnType());
        target.setColumnKey(column.getColumnKey());
        target.setExtra(column.getExtra());
        target.setPrivileges(column.getPrivileges());
        target.setColumnComment(column.getColumnComment());
        target.setGenerationExpression(column.getGenerationExpression());
        target.setSrsId(column.getSrsId());
        return target;
    }
}
