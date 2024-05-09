package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.dto.TableMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class JdbcTypeService {


    @Value("${spring.datasource.driver-class-name}")
    String className;
    @Value("${spring.datasource.url}")
    String url;
    @Value("${spring.datasource.username}")
    String user;
    @Value("${spring.datasource.password}")
    String password;

    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    public void fillJdbcType(TableMessage tableMessage) {
        try {
            // 加载数据库驱动
            Class.forName(className);

            // 建立连接
            conn = DriverManager.getConnection(url, user, password);
            // 创建Statement对象
            stmt = conn.createStatement();
                String format = String.format("SELECT * FROM `%s`.`%s` limit 1",
                        tableMessage.getTableSchema(),
                        tableMessage.getTableName());
                // 不安全，但是只能这么写
                rs = stmt.executeQuery(format);

                // 处理结果
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    int columnType = rs.getMetaData().getColumnType(i + 1);
                    JDBCType jdbcType = JDBCType.valueOf(columnType);
                    tableMessage.getColumns().get(i).setJdbcType(jdbcType.getName());
                }
            // 执行查询


        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
