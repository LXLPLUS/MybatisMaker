package com.lxkplus.mybatisMaker.dto;

import com.lxkplus.mybatisMaker.enums.MapperType;
import lombok.Data;

@Data
public class JpaRow {
    String table;
    String funcName;
    String alias;
    boolean mybatis = true;
    boolean mybatisPlus = true;

    public String getAlias() {
        if (alias == null) {
            return funcName;
        }
        return alias;
    }
}
