package com.lxkplus.mybatisMaker.utils;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;

public class CovertUtils {

    public static String coverToUpperCamel(String str) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (str.contains("_")) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str);
        }
        if (StringUtils.isAllUpperCase(str.substring(0, 1))) {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str);
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str);
    }
}
