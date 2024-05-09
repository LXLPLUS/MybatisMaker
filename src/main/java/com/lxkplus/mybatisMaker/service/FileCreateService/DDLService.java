package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.po.CreateTableDDL;
import com.lxkplus.mybatisMaker.po.ViewDDL;
import com.lxkplus.mybatisMaker.service.PathService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class DDLService implements FileCreateService {
    @Value("${mybatis-maker.datetime-format}")
    String dateTimeFormat;
    SimpleDateFormat dateFormat;
    @Resource
    DatabaseMapper databaseMapper;
    @Resource
    PathService pathService;

    @PostConstruct
    void init() {
        dateFormat = new SimpleDateFormat(dateTimeFormat);
    }

    public void createFile(TableMessage table) throws IOException {
        Date currentDate = new Date();
        String currentTime = dateFormat.format(currentDate);
        String formatDDL = String.format("# %s\nuse %s;\n%s",
                currentTime,
                table.getTableSchema(),
                table.getDDL());

        if (table.getDDL() != null) {
            pathService.createFile(table.getDDLPath(),formatDDL);
        }
    }

    public String getView(TableMessage table) {
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
