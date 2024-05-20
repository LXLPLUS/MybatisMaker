package com.lxkplus.mybatisMaker.service.FileCreateService.Jpa;

import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JpaFilter {

    private boolean starEquals(String ruler, String str) {
        if ("*".equals(ruler)) {
            return true;
        }
        if (str == null || ruler == null) {
            return false;
        }
        return str.equals(ruler);
    }

    public boolean rulerEquals(@NotNull String ruler, TableFlowContext tableFlowContext) {
        if (!ruler.contains("*") || ruler.length() - ruler.replace(".", "").length() > 1) {
            log.warn("规则" + ruler + "不符合标准，不生效");
        }
        String[] split = ruler.split("\\.", 2);
        return starEquals(split[0], tableFlowContext.getTableSchema()) && starEquals(split[1], tableFlowContext.getTableName());
    }
}
