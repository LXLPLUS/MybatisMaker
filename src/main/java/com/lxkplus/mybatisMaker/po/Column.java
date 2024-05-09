package com.lxkplus.mybatisMaker.po;

import lombok.Data;

@Data
public class Column {
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
}
