package com.lxkplus.mybatisMaker.utils;

import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import org.apache.commons.lang3.StringUtils;

public class ColumnSafeUtils {

    /**
     * 以后可能会扩展到其他类型数据库
     * @param column 列名
     * @return 安全的列名
     */
    public static String safeColumn(String column) {
        String trim = StringUtils.strip(column, "`");
        return StringUtils.join("`", trim, "`");
    }

    public static String safeTableName(TableFlowContext tableFlowContext) {
        if (tableFlowContext.isActiveDatabase()) {
            return safeColumn(tableFlowContext.getTableName());
        }
        return safeColumn(tableFlowContext.getTableSchema()) + "." + safeColumn(tableFlowContext.getTableName());
    }
}
