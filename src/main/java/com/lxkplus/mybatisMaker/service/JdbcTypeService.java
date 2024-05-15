package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
@Slf4j
public class JdbcTypeService {

    @Resource
    SqlSessionFactory sqlSessionFactory;

    public void  fillJdbcType(TableFlowContext tableFlowContext) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            // 加载数据库驱动
            DatabaseMetaData metaData = session.getConnection().getMetaData();
            ResultSet tables = metaData.getColumns(null, tableFlowContext.getTableSchema(), tableFlowContext.getTableName(), null);
            int index = 0;
            while (tables.next()) {
                // 防止因为表格临时更改导致异常抛出
                if (index == tableFlowContext.getColumns().size()) {
                    return;
                }
                int columnType = tables.getInt("DATA_TYPE");
                tableFlowContext.getColumns().get(index).setJdbcType(JDBCType.valueOf(columnType).getName());
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
