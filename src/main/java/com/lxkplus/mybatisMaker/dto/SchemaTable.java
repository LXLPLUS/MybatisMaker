package com.lxkplus.mybatisMaker.dto;

import com.lxkplus.mybatisMaker.entity.Column;
import lombok.Data;

@Data
public class SchemaTable {
    String tableSchema;
    String tableName;

    public static SchemaTable convert(Column column) {
        SchemaTable schemaTable = new SchemaTable();
        schemaTable.setTableSchema(column.getTableSchema());
        schemaTable.setTableName(column.getTableName());
        return schemaTable;
    }
}
