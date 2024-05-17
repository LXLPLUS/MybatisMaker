package com.lxkplus.mybatisMaker.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.enums.Package;
import com.lxkplus.mybatisMaker.service.FileCreateService.DDLService;
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
public class TableService {
    @Resource
    MybatisMakerConf mybatisMakerConf;
    @Resource
    JdbcTypeService jdbcTypeService;

    @Resource
    MybatisMakerMybatisConf mybatisMakerMybatisConf;
    @Resource
    DDLService ddlService;
    @Value("${mybatis-maker.package.ddl-package}")
    String DDLPackage;
    @Value("${mybatis-maker.package.mybatis-plus-bean-package}")
    String mybatisPlusPackage;
    @Value("${mybatis-maker.mybatis.jdbc_type}")
    boolean showJdbcType;
    @Value("${mybatis-maker.connect.active_database:null}")
    String activeDatabase;

    @Resource
    TableCompareService tableCompareService;

    HashMap<String, Type> simpleNameMap = new HashMap<>();

    @PostConstruct
    void init() {
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
    PathService pathService;
    private final Random r = new Random();

    private String convertTableNameToJavaBeanName(String schemaName, String tableName, boolean active) {
        tableName = tableName
                .replace(" ", "_")
                .replaceAll(Constants.JAVA_NOT_SUPPORT_CHAR, "");
        if (StringUtils.isBlank(tableName)) {
            return "Random" + r.nextInt();
        }
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

        if (tableFlowContext.isActiveDatabase()) {
            tableFlowContext.setDatabaseWithTableName(tableFlowContext.getTableName());
        } else {
            tableFlowContext.setDatabaseWithTableName(tableFlowContext.getTableSchema() + "." + tableFlowContext.getTableName());
        }

        // 进行名称转化
        String tableName = tableFlowContext.getTableName();
        String tableSchema = tableFlowContext.getTableSchema();
        String beanName = this.convertTableNameToJavaBeanName(tableSchema, tableName, tableFlowContext.isActiveDatabase());
        tableFlowContext.setJavaBeanName(beanName);
        tableFlowContext.setMapperName(tableFlowContext.getJavaBeanName() + "Mapper");
        tableFlowContext.setFullyQualifiedName(mybatisMakerMybatisConf.getMybatisEntityPackage() + "." + tableFlowContext.getJavaBeanName());
        /*
         * package
         */
        tableFlowContext.setMybatisXmlPackage(new Package(mybatisMakerMybatisConf.getXmlPackage()));
        tableFlowContext.setMybatisPackage(new Package(mybatisMakerMybatisConf.getMybatisEntityPackage()));
        tableFlowContext.setMybatisPlusPackage(new Package(mybatisPlusPackage));
        tableFlowContext.setMybatisMapperPackage(new Package(mybatisMakerMybatisConf.getMapperPackage()));
        tableFlowContext.setDDLPackage(new Package(DDLPackage));

        /*
         * Path
         */
        Path xmlPath = pathService.getPath(tableFlowContext.getMybatisXmlPackage(), tableFlowContext.getMapperName() + ".xml");
        tableFlowContext.setMybatisXMLPath(xmlPath);
        Path mybatisPath = pathService.getPath(tableFlowContext.getMybatisPackage(), tableFlowContext.getJavaBeanName() + ".java");
        tableFlowContext.setMybatisEntityPath(mybatisPath);
        Path mybatisPlusPath = pathService.getPath(tableFlowContext.getMybatisPlusPackage(), tableFlowContext.getJavaBeanName() + ".java");
        tableFlowContext.setMybatisPlusEntityPath(mybatisPlusPath);
        Path mapperPath = pathService.getPath(tableFlowContext.getMybatisMapperPackage(), tableFlowContext.getMapperName() + ".java");
        tableFlowContext.setMybatisMapperPath(mapperPath);
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

        tableCompareService.tagNotWatchTime(tableFlowContext);
    }
}
