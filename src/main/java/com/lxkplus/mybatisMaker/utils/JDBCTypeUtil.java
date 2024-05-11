package com.lxkplus.mybatisMaker.utils;

import java.sql.JDBCType;

public class JDBCTypeUtil {
    public static int getJDBCTypeNumber(String jdbcType) {
        return JDBCType.valueOf(jdbcType).getVendorTypeNumber();
    }

    public static int getJdbcTypeNumber(JDBCType jdbcType) {
        return jdbcType.getVendorTypeNumber();
    }
}
