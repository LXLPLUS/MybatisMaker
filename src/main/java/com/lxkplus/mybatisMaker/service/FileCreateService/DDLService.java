package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.CreateTableDDL;
import com.lxkplus.mybatisMaker.entity.ViewDDL;
import com.lxkplus.mybatisMaker.service.PathService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class DDLService implements FileCreateService {
    SimpleDateFormat dateFormat;
    @Resource
    DatabaseMapper databaseMapper;

    @Resource
    MybatisMakerConf mybatisMakerConf;
    @Resource
    PathService pathService;

    @PostConstruct
    void init() {
        dateFormat = new SimpleDateFormat(mybatisMakerConf.getDatetimeFormat());
    }

    public void createFile(TableFlowContext table) throws IOException {
        Date currentDate = new Date();
        String currentTime = dateFormat.format(currentDate);
        String formatDDL = String.format("# %s\nuse %s;\n%s",
                currentTime,
                table.getTableSchema(),
                table.getDDL());

        if (table.getDDL() != null) {
            pathService.createFile(table.getDDLPath(), formatDDL);
        }
    }

    public String getView(TableFlowContext table) {
        String DDL = "";
        if (Objects.equals(table.getTableType(), "BASE TABLE")) {
            CreateTableDDL createDDL = databaseMapper.getCreateDDL(table.getTableSchema(), table.getTableName());
            DDL = createDDL.getCreateTable();
        }
        // 生成view的建表语句
        else if (Objects.equals(table.getTableType(), "VIEW")) {
            ViewDDL viewDDL = databaseMapper.getViewDDL(table.getTableSchema(), table.getTableName());
            DDL = viewDDL.getCreateView();
        }
        return DDL;
    }
}
