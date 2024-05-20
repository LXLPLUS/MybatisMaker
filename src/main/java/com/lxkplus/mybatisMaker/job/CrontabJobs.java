package com.lxkplus.mybatisMaker.job;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.service.*;
import com.lxkplus.mybatisMaker.service.FileCreateService.DDLService;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisEntityService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisMapperService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisXMLService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusEntityService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusMapperService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusXmlService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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
    MybatisEntityService mybatisEntityService;
    @Resource
    DDLService ddlService;
    @Resource
    TableService tableService;
    @Resource
    MybatisPlusEntityService mybatisPlusEntityService;
    @Resource
    MybatisMapperService mybatisMapperService;
    @Resource
    MybatisXMLService mybatisXMLService;

    @Resource
    MybatisPlusMapperService mybatisPlusMapperService;

    @Resource
    MybatisPlusXmlService mybatisPlusXmlService;

    @Resource
    CountService countService;

    @Resource
    PersistenceService persistenceService;

    List<FileCreateService> taskList = new ArrayList<>();
    @PostConstruct
    void init() throws IOException {
        taskList.add(mybatisEntityService);
        taskList.add(mybatisMapperService);
        taskList.add(mybatisXMLService);
        taskList.add(ddlService);
        taskList.add(mybatisPlusEntityService);
        taskList.add(mybatisPlusMapperService);
        taskList.add(mybatisPlusXmlService);

        List<Column> load = persistenceService.load();
        if (!load.isEmpty()) {
            log.info("成功加载缓存，共{}行", load.size());
            cacheService.put(load);
        }

    }

    @Scheduled(initialDelay = 5000L, fixedDelay = 2000L)
    public void flash() throws IOException {
        countService.addCount();
        // 和缓存比较
        List<Column> newColumns = databaseMapper.getColumns();
        // 只保留符合规则的数据
        newColumns = tableCompareService.getSuitColumn(newColumns);
        List<Column> oldColumns = cacheService.get();
        if (!newColumns.equals(oldColumns)) {
            cacheService.put(newColumns);
            countService.change();
            persistenceService.save(newColumns);
        } else if (countService.timeFromLastCheck(Constants.TIME_INFO)) {
            log.info("数据库表格{}秒没发生变更", countService.timeFromLastChange() / 1000);
            countService.check();
            return;
        } else {
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
                for (FileCreateService fileCreateService : taskList) {
                    if (fileCreateService.generate()) {
                        fileCreateService.createFile(table);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
