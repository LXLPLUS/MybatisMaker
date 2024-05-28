package com.lxkplus.mybatisMaker.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.conf.MybatisPlusConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.JpaRow;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.enums.Package;
import com.lxkplus.mybatisMaker.service.FileCreateService.DDLService;
import com.lxkplus.mybatisMaker.service.FileCreateService.Jpa.JpaFilter;
import com.lxkplus.mybatisMaker.service.FileCreateService.Jpa.JpaLoadService;
import com.lxkplus.mybatisMaker.service.FileCreateService.Jpa.JpaService;
import com.lxkplus.mybatisMaker.utils.ColumnSafeUtils;
import com.lxkplus.mybatisMaker.utils.CovertUtils;
import com.lxkplus.mybatisMaker.utils.JDBCTypeUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.*;

@Service
@Slf4j
public class TableHandleService {

    @Resource
    private JpaLoadService jpaLoadService;

    @Resource
    private JpaFilter jpaFilter;

    @Resource
    private JpaService jpaService;
    @Resource
    private MybatisMakerConf mybatisMakerConf;
    @Resource
    private JdbcTypeService jdbcTypeService;

    @Resource
    private MybatisMakerMybatisConf mybatisMakerMybatisConf;
    @Resource
    private DDLService ddlService;
    @Value("${mybatis-maker.package.ddl-package}")
    private String DDLPackage;
    @Value("${mybatis-maker.connect.active_database:null}")
    private String activeDatabase;

    @Resource
    private MybatisPlusConf mybatisPlusConf;

     HashMap<String, Type> simpleNameMap = new HashMap<>();

    @PostConstruct
    private void init() {
        simpleNameMap.put(String.class.getSimpleName(), String.class);
        simpleNameMap.put(int.class.getSimpleName(), int.class);
        simpleNameMap.put(long.class.getSimpleName(), long.class);
        simpleNameMap.put(char.class.getSimpleName(), char.class);
        simpleNameMap.put(float.class.getSimpleName(), float.class);
        simpleNameMap.put(double.class.getSimpleName(), double.class);
        simpleNameMap.put(boolean.class.getSimpleName(), boolean.class);
        simpleNameMap.put(short.class.getSimpleName(), short.class);
        simpleNameMap.put(byte.class.getSimpleName(), byte.class);

        simpleNameMap.put(Long.class.getSimpleName(), Long.class);
        simpleNameMap.put(Boolean.class.getSimpleName(), Boolean.class);
        simpleNameMap.put(Integer.class.getSimpleName(), Integer.class);
        simpleNameMap.put(Float.class.getSimpleName(), Float.class);
        simpleNameMap.put(Double.class.getSimpleName(), Double.class);
        simpleNameMap.put(Short.class.getSimpleName(), Short.class);
        simpleNameMap.put(Character.class.getSimpleName(), Character.class);
        simpleNameMap.put(Short.class.getSimpleName(), Short.class);

        simpleNameMap.put(byte[].class.getSimpleName(), byte[].class);
        simpleNameMap.put(char[].class.getSimpleName(), char[].class);
        simpleNameMap.put(int[].class.getSimpleName(), int[].class);
        simpleNameMap.put(LocalDateTime.class.getSimpleName(), LocalDateTime.class);
        simpleNameMap.put(String.class.getSimpleName().toLowerCase(), String.class);
        simpleNameMap.put(Blob.class.getSimpleName(), Blob.class);
        simpleNameMap.put(LocalDateTime.class.getSimpleName(), LocalDateTime.class);
        simpleNameMap.put(LocalDate.class.getSimpleName(), LocalDate.class);
        simpleNameMap.put(LocalTime.class.getSimpleName(), LocalTime.class);
        simpleNameMap.put(Clob.class.getSimpleName(), Clob.class);
        simpleNameMap.put(BigDecimal.class.getSimpleName(), BigDecimal.class);
        simpleNameMap.put(Date.class.getSimpleName(), Date.class);
        simpleNameMap.put(Time.class.getSimpleName(), Time.class);
        simpleNameMap.put(Timestamp.class.getSimpleName(), Timestamp.class);
    }


    @Resource
    private PathService pathService;
    private final Random r = new Random();

    private String convertTableNameToJavaBeanName(String schemaName, String tableName, boolean active) {
        tableName = tableName
                .replace(" ", "_")
                .replaceAll(Constants.JAVA_NOT_SUPPORT_CHAR, "");
        if (tableName.charAt(0) >= '0' && tableName.charAt(0) <= '9') {
            return "Number" + tableName;
        }
        List<String> trimFirst = mybatisMakerConf.getTrimFirst();
        String tableNameTrim =  tableName;
        // 移除所有前后缀
        // 只移除一次
        for (String head : trimFirst) {
            tableNameTrim = StringUtils.removeStart(tableNameTrim, head);
            break;
        }
        for (String tail : mybatisMakerConf.getTrimLast()) {
            tableNameTrim = StringUtils.removeEnd(tableNameTrim, tail);
            break;
        }
        if (StringUtils.isBlank(tableName)) {
            return "Random" + r.nextInt();
        }
        if (active) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableNameTrim);
        }
        return CovertUtils.coverToUpperCamel(schemaName) + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableNameTrim);
    }

    public void fillMessage(TableFlowContext tableFlowContext) throws ClassNotFoundException, SQLException {

        // 是否带数据库前缀
        if (tableFlowContext.getTableSchema().equals(activeDatabase)) {
            tableFlowContext.setActiveDatabase(true);
        }
        tableFlowContext.setSafeTableName(ColumnSafeUtils.safeTableName(tableFlowContext));

        // 进行名称转化
        String tableName = tableFlowContext.getTableName();
        String tableSchema = tableFlowContext.getTableSchema();
        String beanName = this.convertTableNameToJavaBeanName(tableSchema, tableName, tableFlowContext.isActiveDatabase());
        tableFlowContext.setJavaBeanName(beanName);
        tableFlowContext.setMapperName(tableFlowContext.getJavaBeanName() + "Mapper");
        tableFlowContext.setFullyQualifiedName(mybatisMakerMybatisConf.getMybatisEntityPackage() + "." + tableFlowContext.getJavaBeanName());
        /*
         * mybatis package
         */
        tableFlowContext.setMybatisXmlPackage(new Package(mybatisMakerMybatisConf.getXmlPackage()));
        tableFlowContext.setMybatisBeanPackage(new Package(mybatisMakerMybatisConf.getMybatisEntityPackage()));
        tableFlowContext.setMybatisMapperPackage(new Package(mybatisMakerMybatisConf.getMapperPackage()));

        /*
            mybatis-plus package
         */
        tableFlowContext.setMybatisPlusPackage(new Package(mybatisPlusConf.getMybatisPlusBeanPackage()));
        tableFlowContext.setMybatisPlusMapperPackage(new Package(mybatisPlusConf.getMapperPackage()));
        tableFlowContext.setMybatisPlusXmlPackage(new Package(mybatisPlusConf.getXmlPackage()));

        tableFlowContext.setDDLPackage(new Package(DDLPackage));

        /*
         * mybatis Path
         */
        Path xmlPath = pathService.getPath(tableFlowContext.getMybatisXmlPackage(), tableFlowContext.getMapperName() + ".xml");
        tableFlowContext.setMybatisXmlPath(xmlPath);
        Path mybatisPath = pathService.getPath(tableFlowContext.getMybatisBeanPackage(), tableFlowContext.getJavaBeanName() + ".java");
        tableFlowContext.setMybatisEntityPath(mybatisPath);
        Path mapperPath = pathService.getPath(tableFlowContext.getMybatisMapperPackage(), tableFlowContext.getMapperName() + ".java");
        tableFlowContext.setMybatisMapperPath(mapperPath);

        /*
         * mybatis-plus Path
         */
        Path mybatisPlusPath = pathService.getPath(tableFlowContext.getMybatisPlusPackage(), tableFlowContext.getJavaBeanName() + ".java");
        tableFlowContext.setMybatisPlusEntityPath(mybatisPlusPath);
        Path mybatisPlusMapperPath = pathService.getPath(tableFlowContext.getMybatisPlusMapperPackage(), tableFlowContext.getMapperName() + ".java");
        tableFlowContext.setMybatisPlusMapperPath(mybatisPlusMapperPath);
        Path mybatisPlusXmlPath = pathService.getPath(tableFlowContext.getMybatisPlusXmlPackage(), tableFlowContext.getMapperName() + ".xml");
        tableFlowContext.setMybatisPlusXmlPath(mybatisPlusXmlPath);

        Path ddlPath = pathService.getPath(tableFlowContext.getDDLPackage(), tableFlowContext.getTableSchema() + "-" + tableFlowContext.getJavaBeanName() + ".sql");
        tableFlowContext.setDDLPath(ddlPath);

        tableFlowContext.setMybatisResultMapId(tableFlowContext.getJavaBeanName() + "Map");


        // 生成基本表的建表语句
        String DDL = ddlService.getView(tableFlowContext);
        DDL = DDL.replaceAll("AUTO_INCREMENT=\\d+", "AUTO_INCREMENT=0");
        tableFlowContext.setDDL(DDL + ";");

        List<ColumnWithJavaStatus> columns = tableFlowContext.getColumns();
        columns.sort(Comparator.comparing(ColumnWithJavaStatus::getOrdinalPosition));

        // 添加jDBCType映射
        jdbcTypeService.fillJdbcType(tableFlowContext);

        // 通过配置文件实现类型映射
        Map<String, String> typeMapper = mybatisMakerConf.getTypeMapper();

        for (ColumnWithJavaStatus column : tableFlowContext.getColumns()) {
            // 转化为安全的sql
            column.setSafeColumnName(ColumnSafeUtils.safeColumn(column.getColumnName()));

            // 类型映射
            if (simpleNameMap.containsKey(typeMapper.get(column.getJdbcType().getName()))) {
                Type type = simpleNameMap.get(typeMapper.get(column.getJdbcType().getName()));
                column.setJavaType(type);
            } else if (column.getJdbcType() == null) {
                column.setJavaType(String.class);
            } else {
                column.setJavaType(Class.forName(typeMapper.get(column.getJdbcType().getName())));
            }
        }

        // 获取插入表格
        for (ColumnWithJavaStatus column : tableFlowContext.getColumns()) {
            if (column.getOrdinalPosition() == 1 && "PRI".equals(column.getColumnKey())) {
                tableFlowContext.setIdColumn(column);
                if (JDBCTypeUtil.EqualsAny(column.getJdbcType(), JDBCType.INTEGER, JDBCType.BIGINT)) {
                    tableFlowContext.setIdType(IdType.AUTO);
                } else if (JDBCTypeUtil.EqualsAny(column.getJdbcType(), JDBCType.VARCHAR, JDBCType.CHAR)) {
                    tableFlowContext.setIdType(IdType.ASSIGN_UUID);
                }
                break;
            }
        }

        for (JpaRow jpaRow : jpaLoadService.getJpaRowList()) {
            if (jpaFilter.rulerEquals(jpaRow.getTable(), tableFlowContext)) {
                jpaService.convertToMybatis(tableFlowContext, jpaRow);
            }
        }
    }
}
