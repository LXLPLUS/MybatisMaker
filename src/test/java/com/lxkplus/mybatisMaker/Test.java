package com.lxkplus.mybatisMaker;

import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

@SpringBootTest
public class Test {

    @Resource
    SqlSessionFactory sqlSessionFactory;
    

    @org.junit.jupiter.api.Test
    public void test() throws SQLException {
    }
}
