package com.lxkplus.mybatisMaker.service;

import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.po.Column;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ColumnService{

    public ColumnWithJavaStatus explain(Column column) {
        ColumnWithJavaStatus convert = ColumnWithJavaStatus.convert(column);

        // 格式化mysql名 -> java 列名
        String columnName = column.getColumnName()
                .replace(" ", "_")
                .replaceAll(Constants.JAVA_NOT_SUPPORT_CHAR, "");
        if (StringUtils.isBlank(columnName)) {
            columnName = "undefined_" + column.getOrdinalPosition();
        } else if (columnName.charAt(0) >= '0' && columnName.charAt(0) <= '9') {
            columnName = "number_" + columnName;
        } else {
            columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
            convert.setConvertMysql2JavaStatus(true);
        }
        convert.setJavaColumnName(columnName);

        return convert;
    }

}
