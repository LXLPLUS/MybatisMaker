package com.lxkplus.mybatisMaker.job;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.po.Column;
import com.lxkplus.mybatisMaker.service.CacheService;
import com.lxkplus.mybatisMaker.service.FileCreateService.*;
import com.lxkplus.mybatisMaker.service.TableCompareService;
import com.lxkplus.mybatisMaker.service.TableService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class CrontabJobs {

    @Resource
    DatabaseMapper databaseMapper;
    @Resource
    CacheService cacheService;
    @Resource
    TableCompareService tableCompareService;
    @Resource
    MybatisService mybatisService;
    @Resource
    DDLService ddlService;
    @Resource
    TableService tableService;
    @Resource
    MybatisPlusService mybatisPlusService;
    @Resource
    MapperService mapperService;
    @Resource
    MybatisXMLService mybatisXMLService;

    long changeTimeStamp = System.currentTimeMillis();

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(initialDelay = 5000L, fixedDelay = 2000L)
    public void Flash() {

        LocalDateTime now = LocalDateTime.now();

        MDC.put("time", now.format(dateTimeFormatter));
        // 和缓存比较
        List<Column> newColumns = databaseMapper.getColumns();
        // 只保留符合规则的数据
        newColumns = tableCompareService.getSuitColumn(newColumns);
        List<Column> oldColumns = cacheService.get();
        if (!newColumns.equals(oldColumns)) {
            cacheService.put(newColumns);
            changeTimeStamp = System.currentTimeMillis();
        } else {
            long notChangeTimeStamp = (System.currentTimeMillis() - changeTimeStamp) / 1000;
            if (notChangeTimeStamp > 0 && notChangeTimeStamp % Constants.TIME_INFO == 0) {
                log.info("数据库表格{}秒没发生变更", notChangeTimeStamp);
            }
        }

        // 对数据进行处理，防止异常
        try {
            if (!oldColumns.equals(newColumns)) {
                // 打印删除和删除的信息
                tableCompareService.infoCreateAndDelete(oldColumns, newColumns);
                // 获取有变更的表格，并填入列信息
                List<TableMessage> tables = tableCompareService.getCreateOrDiffTable(oldColumns, newColumns);
                tables.forEach(x -> {
                    try {
                        tableService.fillMessage(x);
                    } catch (ClassNotFoundException | SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                for (TableMessage table : tables) {
                    tableService.fillMessage(table);
                    mybatisService.createFile(table);
                    mybatisPlusService.createFile(table);
                    mapperService.createFile(table);
                    ddlService.createFile(table);
                    mybatisXMLService.createFile(table);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
