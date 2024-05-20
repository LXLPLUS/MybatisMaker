package com.lxkplus.mybatisMaker.Mapper;

import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.entity.CreateTableDDL;
import com.lxkplus.mybatisMaker.entity.InformationSchemaTables;
import com.lxkplus.mybatisMaker.entity.ViewDDL;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DatabaseMapper {
    List<Column> getColumns();

    CreateTableDDL getCreateDDL(@Param("databaseName") String databaseName, @Param("tableName") String tableName);

    ViewDDL getViewDDL(@Param("databaseName") String databaseName, @Param("tableName") String tableName);

    InformationSchemaTables getDatabaseType(@Param("databaseName") String databaseName, @Param("tableName") String tableName);
}
