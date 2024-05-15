package com.lxkplus.mybatisMaker.job;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.service.CacheService;
import com.lxkplus.mybatisMaker.service.FileCreateService.*;
import com.lxkplus.mybatisMaker.service.TableCompareService;
import com.lxkplus.mybatisMaker.service.TableService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CrontabJobs {

    @Resource
    MybatisMakerConf mybatisMakerConf;

    @Resource
    DatabaseMapper databaseMapper;
    @Resource
    CacheService cacheService;
    @Resource
    TableCompareService tableCompareService;
    @Resource
    MybatisEntityService mybatisEntityService;
    @Resource
    DDLService ddlService;
    @Resource
    TableService tableService;
    @Resource
    MybatisPlusEntityService mybatisPlusEntityService;
    @Resource
    MapperService mapperService;
    @Resource
    MybatisXMLService mybatisXMLService;


    List<FileCreateService> taskList = new ArrayList<>();
    @PostConstruct
    void init() {
        taskList.add(mybatisEntityService);
        taskList.add(mybatisPlusEntityService);
        taskList.add(mapperService);
        taskList.add(ddlService);
        taskList.add(mybatisXMLService);
    }

    long changeTimeStamp = System.currentTimeMillis();

    int count = 0;

    boolean firstRun = true;

    @Scheduled(initialDelay = 5000L, fixedDelay = 2000L)
    public void Flash() {
        count++;
        MDC.put("count", Integer.toString(count));
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
            return;
        }

        // 对数据进行处理，防止异常
        try {
            // 打印删除和删除的信息
            tableCompareService.infoCreateAndDelete(oldColumns, newColumns);
            // 获取有变更的表格，并填入列信息
            List<TableFlowContext> tables = tableCompareService.getCreateOrDiffTable(oldColumns, newColumns);

            for (TableFlowContext table : tables) {
                tableService.fillMessage(table);

                // 只执行一次
                if (firstRun && mybatisMakerConf.isClearHistory()) {
                    for (FileCreateService fileCreateService : taskList) {
                        fileCreateService.deleteFile(table);
                    }
                    firstRun = false;
                }
                for (FileCreateService fileCreateService : taskList) {
                    fileCreateService.createFile(table);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
