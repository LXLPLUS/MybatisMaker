package com.lxkplus.mybatisMaker.enums;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class Package  {

    String packageName;

    public String getPackageName() {
        return StringUtils.replace(packageName, "\"", "");
    }


}
