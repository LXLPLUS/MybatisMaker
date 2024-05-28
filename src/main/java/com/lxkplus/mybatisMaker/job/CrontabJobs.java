package com.lxkplus.mybatisMaker.job;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.manager.CacheManager;
import com.lxkplus.mybatisMaker.manager.CountManager;
import com.lxkplus.mybatisMaker.manager.PersistenceManager;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CrontabJobs {

    @Resource
    DatabaseMapper databaseMapper;
    @Resource
    CacheManager cacheManager;
    @Resource
    TableCompareService tableCompareService;

    @Resource
    CountManager countManager;

    @Resource
    PersistenceManager persistenceManager;

    List<FileCreateService> taskList = new ArrayList<>();

    @Resource
    AllTaskCollectService allTaskCollectService;

    @PostConstruct
    void init() throws IOException {
        taskList = allTaskCollectService.getFileCreateServiceList();

        List<Column> load = persistenceManager.load();
        if (!load.isEmpty()) {
            log.info("成功加载缓存，共{}行", load.size());
            cacheManager.put(load);
        }

    }

    @Scheduled(initialDelay = 5000L, fixedDelay = 2000L)
    public void flash() throws IOException, SQLException, ClassNotFoundException {
        countManager.addCount();
        // 和缓存比较
        List<Column> newColumns = databaseMapper.getColumns();
        // 只保留符合规则的数据
        newColumns = tableCompareService.getSuitColumn(newColumns);
        List<Column> oldColumns = cacheManager.get();
        if (!newColumns.equals(oldColumns)) {
            cacheManager.put(newColumns);
            countManager.change();
            persistenceManager.save(newColumns);
        } else if (countManager.timeLongerThanLastCheck(Constants.TIME_INFO)) {
            log.info("数据库表格{}秒没发生变更", countManager.timeFromLastChange() / 1000);
            countManager.check();
            return;
        } else {
            return;
        }

        // 打印删除和删除的信息
        tableCompareService.infoCreateAndDelete(oldColumns, newColumns);
        // 获取有变更的表格，并填入列信息
        List<TableFlowContext> tables = tableCompareService.getCreateOrDiffTable(oldColumns, newColumns);

        for (TableFlowContext table : tables) {
            for (FileCreateService fileCreateService : taskList) {
                if (fileCreateService.needGenerate()) {
                    try {
                        fileCreateService.createFile(table);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
