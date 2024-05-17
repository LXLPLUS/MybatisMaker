package com.lxkplus.mybatisMaker.utils;

import java.sql.JDBCType;

public class JDBCTypeUtil {

    public static boolean EqualsAny(JDBCType result, JDBCType... jdbcType) {
        for (JDBCType type : jdbcType) {
            if (result.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
