package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.conf.SyncConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.SuitRuler;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.entity.InformationSchemaTables;
import com.lxkplus.mybatisMaker.utils.JDBCTypeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.*;

@Component
@Slf4j
public class TableCompareService {

    @Resource
    SyncConf syncConf;
    @Resource
    ColumnService columnService;
    @Resource
    private DatabaseMapper databaseMapper;
    List<SuitRuler> tableAllowList = new ArrayList<>();
    List<SuitRuler> tableNotAllowList = new ArrayList<>();

    @Resource
    MybatisMakerMybatisConf mybatisMakerMybatisConf;


    private boolean starEquals(String str, String ruler) {
        if ("*".equals(ruler)) {
            return true;
        }
        if (str == null || ruler == null) {
            return false;
        }
        return str.equals(ruler);
    }
    @PostConstruct
    void init() {
        for (String ruler : syncConf.getSyncTable()) {
            registerRuler(ruler, tableAllowList);
        }

        for (String ruler : syncConf.getExceptTable()) {
            registerRuler(ruler, tableNotAllowList);
        }
    }

    private void registerRuler(String ruler, List<SuitRuler> tableNotAllowList) {
        if (!ruler.contains("*") || ruler.length() - ruler.replace(".", "").length() > 1) {
            log.warn("规则" + ruler + "不符合标准，不生效");
        }
        String[] split = ruler.split("\\.", 2);
        SuitRuler suitRuler = new SuitRuler();
        suitRuler.setTableSchema(split[0]);
        suitRuler.setTableName(split[1]);
        tableNotAllowList.add(suitRuler);
    }

    public List<Column> getCreateColumn(List<Column> old, List<Column> newColumn) {
        return getDiffColumns(old, newColumn);
    }

    public List<Column> getDeleteColumn(List<Column> old, List<Column> newColumn) {
        return getDiffColumns(newColumn, old);
    }

    public void infoCreateAndDelete(List<Column> old, List<Column> newColumn) {
        List<Column> create = this.getCreateColumn(old, newColumn);
        List<Column> delete = this.getDeleteColumn(old, newColumn);
        create.forEach(x -> log.info("新建的列: {}", x));
        delete.forEach(x -> log.info("删除的列: {}", x));
    }

    private List<Column> getDiffColumns(List<Column> old, List<Column> newColumn) {
        HashSet<Column> oldColumnSet = new HashSet<>(old);
        List<Column> objects = new ArrayList<>();
        for (Column column : newColumn) {
            if (!oldColumnSet.contains(column)) {
                objects.add(column);
            }
        }
        return objects;
    }

    public List<TableMessage> getCreateOrDiffTable(List<Column> old, List<Column> newColumn) {
        // 在新的表格列表里面存在，而且和old有diff
        // 新的列表里面存在
        HashSet<TableMessage> existsTables = new HashSet<>();
        for (Column column : newColumn) {
            TableMessage tableMessage = new TableMessage();
            BeanUtils.copyProperties(column, tableMessage);
            existsTables.add(tableMessage);
        }
        // 获取现存的所有列
        List<Column> create = getCreateColumn(old, newColumn);
        HashSet<TableMessage> tableCreateDiff = new HashSet<>();
        for (Column column : create) {
            TableMessage tableMessage = new TableMessage();
            BeanUtils.copyProperties(column, tableMessage);
            tableCreateDiff.add(tableMessage);
        }

        // 获取历史的所有列
        List<Column> delete = getDeleteColumn(old, newColumn);
        HashSet<TableMessage> tableDeleteDiff = new HashSet<>();
        for (Column column : delete) {
            TableMessage tableMessage = new TableMessage();
            BeanUtils.copyProperties(column, tableMessage);
            tableDeleteDiff.add(tableMessage);
        }

        // 如果存在并且有diff，那么直接写入
        ArrayList<TableMessage> diffTables = new ArrayList<>();
        for (TableMessage tableMessage : existsTables) {
            if (tableCreateDiff.contains(tableMessage) || tableDeleteDiff.contains(tableMessage)) {
                diffTables.add(tableMessage);
            }
        }

        // 写入表格类型，防止类型错误
        for (TableMessage diffTable : diffTables) {
            InformationSchemaTables databaseType = databaseMapper.getDatabaseType(diffTable.getTableSchema(), diffTable.getTableName());
            diffTable.setTableType(databaseType.getTableType());
            diffTable.setTableComment(databaseType.getTableComment());
            diffTable.setEngine(databaseType.getEngine());
        }

        // 将列挂载对应的表上
        for (Column column : newColumn) {
            for (TableMessage diffTable : diffTables) {
                if (Objects.equals(column.getTableSchema(), diffTable.getTableSchema())
                        && Objects.equals(column.getTableName(), diffTable.getTableName())) {

                    ColumnWithJavaStatus explain = columnService.explain(column);
                    diffTable.getColumns().add(explain);
                    break;
                }
            }
        }

        return diffTables;
    }


    public void moveNotWatchTime(TableMessage tableMessage) {
        List<ColumnWithJavaStatus> dateTimeNotWatch = new ArrayList<>();

        for (ColumnWithJavaStatus column : tableMessage.getColumns()) {
            if (mybatisMakerMybatisConf.getDatetimeNotShow().contains(column.getColumnName()) &&
            Set.of(Types.DATE, Types.TIME, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP).contains(JDBCTypeUtil.getJDBCTypeNumber(column.getJdbcType())) &&
            column.getColumnDefault() != null) {
                dateTimeNotWatch.add(column);
            }
        }
        tableMessage.setDateTimeAutoColumns(dateTimeNotWatch);
    }




    public List<Column> getSuitColumn(List<Column> columns) {
        // 保留的留下
        HashSet<Column> objects = new HashSet<>();
        for (Column column : columns) {
            for (SuitRuler suitRuler : tableAllowList) {
                if (starEquals(column.getTableSchema(), suitRuler.getTableSchema())
                        && starEquals(column.getColumnName(), suitRuler.getTableName())) {
                    objects.add(column);
                    break;
                }
            }
        }

        // 不符合条件的删除
        LinkedHashSet<Column> suitTables = new LinkedHashSet<>(objects);
        for (Column column : columns) {
            for (SuitRuler suitRuler : tableNotAllowList) {
                if (starEquals(column.getTableSchema(), suitRuler.getTableSchema())
                        && starEquals(column.getColumnName(), suitRuler.getTableName())) {
                    suitTables.remove(column);
                    break;
                }
            }
        }
        return new ArrayList<>(suitTables);
    }

}
