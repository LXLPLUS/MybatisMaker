package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.conf.SyncConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.SuitRuler;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.entity.InformationSchemaTables;
import com.lxkplus.mybatisMaker.utils.JDBCTypeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
        HashSet<Column> oldColumnSet = new HashSet<>(old);
        return newColumn.stream().filter(x -> !oldColumnSet.contains(x)).toList();
    }

    public List<Column> getDeleteColumn(List<Column> old, List<Column> newColumn) {
        HashSet<Column> newColumnSet = new HashSet<>(newColumn);
        return old.stream().filter(x -> !newColumnSet.contains(x)).toList();
    }

    public void infoCreateAndDelete(List<Column> old, List<Column> newColumn) {
        List<Column> create = this.getCreateColumn(old, newColumn);
        List<Column> delete = this.getDeleteColumn(old, newColumn);
        create.forEach(x -> log.info("新建的列: {}", x));
        delete.forEach(x -> log.info("删除的列: {}", x));
    }

    public List<TableFlowContext> getCreateOrDiffTable(List<Column> old, List<Column> newColumn) {

        // 获取新表和旧表的所有diff列
        LinkedHashSet<Column> oldSet = new LinkedHashSet<>(old);
        LinkedHashSet<Column> newSet = new LinkedHashSet<>(newColumn);
        LinkedHashSet<Column> allData = new LinkedHashSet<>(newSet);
        allData.addAll(oldSet);
        allData.removeIf(x -> oldSet.contains(x) && newSet.contains(x));

        // 获取存在diff的表格并填充基本信息
        LinkedHashSet<TableFlowContext> diffTables = new LinkedHashSet<>();
        for (Column allDatum : allData) {
            diffTables.add(TableFlowContext.fromColumn(allDatum));
        }

        // 写入表格类型，防止类型错误
        for (TableFlowContext diffTable : diffTables) {
            InformationSchemaTables databaseType = databaseMapper.getDatabaseType(diffTable.getTableSchema(), diffTable.getTableName());
            diffTable.setTableType(databaseType.getTableType());
            diffTable.setTableComment(databaseType.getTableComment());
            diffTable.setEngine(databaseType.getEngine());
        }

        // 将列挂载对应的表上
        for (Column column : newColumn) {
            List<TableFlowContext> list = diffTables.stream()
                    .filter(x -> Objects.equals(column.getTableSchema(), x.getTableSchema()) && Objects.equals(column.getTableName(), x.getTableName()))
                    .toList();
            if (list.isEmpty()) {
                continue;
            }
            ColumnWithJavaStatus explain = columnService.explain(column);
            list.get(0).getColumns().add(explain);
        }

        return new ArrayList<>(diffTables);
    }


    /**
     * 将不侦听的列添加进DateTimeAutoColumn
     * 当其他业务发现DateTimeAutoColumn中存在对应的列的时候，直接跳过
     * @param tableFlowContext 表格数据采集
     */
    public void tagNotWatchTime(TableFlowContext tableFlowContext) {
        List<ColumnWithJavaStatus> dateTimeNotWatch = new ArrayList<>();

        for (ColumnWithJavaStatus column : tableFlowContext.getColumns()) {
            if (mybatisMakerMybatisConf.getDatetimeAutoInsertUpdate().contains(column.getColumnName()) &&
            Set.of(Types.DATE, Types.TIME, Types.TIME_WITH_TIMEZONE, Types.TIMESTAMP).contains(JDBCTypeUtil.getJDBCTypeNumber(column.getJdbcType())) &&
            column.getColumnDefault() != null) {
                dateTimeNotWatch.add(column);
            }
        }
        tableFlowContext.setDateTimeAutoColumns(dateTimeNotWatch);
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
