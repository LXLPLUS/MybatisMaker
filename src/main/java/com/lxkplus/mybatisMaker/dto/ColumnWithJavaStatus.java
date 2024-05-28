package com.lxkplus.mybatisMaker.dto;

import com.lxkplus.mybatisMaker.entity.Column;
import lombok.Data;

import java.lang.reflect.Type;
import java.sql.JDBCType;

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
    JDBCType jdbcType;
    boolean convertMysql2JavaStatus;
    Type javaType;
    String safeColumnName;
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


    @Override
    public ColumnWithJavaStatus clone() {
        ColumnWithJavaStatus columnWithJavaStatus = new ColumnWithJavaStatus();
        columnWithJavaStatus.setTableCatalog(this.getTableCatalog());
        columnWithJavaStatus.setTableSchema(this.getTableSchema());
        columnWithJavaStatus.setTableName(this.getTableName());
        columnWithJavaStatus.setColumnName(this.getColumnName());
        columnWithJavaStatus.setOrdinalPosition(this.getOrdinalPosition());
        columnWithJavaStatus.setColumnDefault(this.getColumnDefault());
        columnWithJavaStatus.setIsNullable(this.getIsNullable());
        columnWithJavaStatus.setDataType(this.getDataType());
        columnWithJavaStatus.setCharacterMaximumLength(this.getCharacterMaximumLength());
        columnWithJavaStatus.setCharacterOctetLength(this.getCharacterOctetLength());
        columnWithJavaStatus.setNumericPrecision(this.getNumericPrecision());
        columnWithJavaStatus.setNumericScale(this.getNumericScale());
        columnWithJavaStatus.setDatetimePrecision(this.getDatetimePrecision());
        columnWithJavaStatus.setCharacterSetName(this.getCharacterSetName());
        columnWithJavaStatus.setCollationName(this.getCollationName());
        columnWithJavaStatus.setColumnType(this.getColumnType());
        columnWithJavaStatus.setColumnKey(this.getColumnKey());
        columnWithJavaStatus.setExtra(this.getExtra());
        columnWithJavaStatus.setPrivileges(this.getPrivileges());
        columnWithJavaStatus.setColumnComment(this.getColumnComment());
        columnWithJavaStatus.setGenerationExpression(this.getGenerationExpression());
        columnWithJavaStatus.setSrsId(this.getSrsId());
        columnWithJavaStatus.setJavaColumnName(this.getJavaColumnName());
        columnWithJavaStatus.setIdType(this.getIdType());
        columnWithJavaStatus.setJdbcType(this.getJdbcType());
        columnWithJavaStatus.setConvertMysql2JavaStatus(this.isConvertMysql2JavaStatus());
        columnWithJavaStatus.setJavaType(this.getJavaType());
        columnWithJavaStatus.setSafeColumnName(this.getSafeColumnName());
        columnWithJavaStatus.setCheckDateTime(this.isCheckDateTime());
        columnWithJavaStatus.setPri(this.isPri());
        return columnWithJavaStatus;
    }
}
