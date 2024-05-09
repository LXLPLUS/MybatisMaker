package com.lxkplus.mybatisMaker.Mapper;

import com.lxkplus.mybatisMaker.po.Column;
import com.lxkplus.mybatisMaker.po.CreateTableDDL;
import com.lxkplus.mybatisMaker.po.InformationSchemaTables;
import com.lxkplus.mybatisMaker.po.ViewDDL;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DatabaseMapper {
    List<Column> getColumns();

    @Select("show create table ${databaseName}.${tableName}")
    @Results({
        @Result(property = "createTable", column = "Create Table")
    })
    CreateTableDDL getCreateDDL(String databaseName, String tableName);

    @Select("show create view ${databaseName}.${tableName}")
    @Results({
            @Result(property = "createView", column = "Create View")
    })
    ViewDDL getViewDDL(String databaseName, String tableName);

    InformationSchemaTables getDatabaseType(@Param("databaseName") String databaseName, @Param("tableName") String tableName);
}
