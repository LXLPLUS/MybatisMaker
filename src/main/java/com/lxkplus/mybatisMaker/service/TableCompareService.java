package com.lxkplus.mybatisMaker.service;

import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.Mapper.DatabaseMapper;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.conf.SyncConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.SuitRuler;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.entity.Column;
import com.lxkplus.mybatisMaker.entity.InformationSchemaTables;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.utils.JDBCTypeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.JDBCType;
import java.util.*;

@Component
@Slf4j
public class TableCompareService {

    @Resource
    SyncConf syncConf;
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

    public void infoCreateAndDelete(List<Column> old, List<Column> newColumn) {
        Collection<Column> create = CollectionUtils.subtract(newColumn, old);
        Collection<Column> delete = CollectionUtils.subtract(old, newColumn);
        create.forEach(x -> log.info("新建的列: {}", x));
        delete.forEach(x -> log.info("删除的列: {}", x));
    }

    public List<TableFlowContext> getCreateOrDiffTable(List<Column> old, List<Column> newColumn) {

        // 获取新表和旧表的所有diff列
        Collection<Column> disjunction = CollectionUtils.disjunction(old, newColumn);

        // 获取存在diff的表格并填充基本信息
        LinkedHashSet<TableFlowContext> diffTables = new LinkedHashSet<>();
        for (Column allDatum : disjunction) {
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
        if (!list.isEmpty()) {
            ColumnWithJavaStatus explain = this.explain(column);
            list.get(0).getColumns().add(explain);
            }
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
            JDBCTypeUtil.EqualsAny(column.getJdbcType(), JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP, JDBCType.TIME_WITH_TIMEZONE, JDBCType.TIMESTAMP_WITH_TIMEZONE) &&
            column.getColumnDefault() != null) {
                dateTimeNotWatch.add(column);
            }
        }
        tableFlowContext.setDateTimeAutoColumns(dateTimeNotWatch);
    }

    public List<Column> getSuitColumn(List<Column> columns) {
        // 保留的留下
        HashSet<Column> objects = new HashSet<>(columns);
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

    public ColumnWithJavaStatus explain(Column column) {
        ColumnWithJavaStatus convert = ColumnWithJavaStatus.convert(column);

        // 格式化mysql名 -> java 列名
        String columnName = column.getColumnName()
                .replace(" ", "_")
                .replaceAll(Constants.JAVA_NOT_SUPPORT_CHAR, "");
        if (StringUtils.isBlank(columnName)) {
            columnName = "undefined_" + column.getOrdinalPosition();
        } else if (columnName.charAt(0) >= '0' && columnName.charAt(0) <= '9') {
            columnName = "number_" + columnName;
        } else {
            columnName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnName);
            convert.setConvertMysql2JavaStatus(true);
        }
        convert.setJavaColumnName(columnName);

        return convert;
    }

}
