package com.lxkplus.mybatisMaker.service;

import com.baomidou.mybatisplus.annotation.IdType;
import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.enums.Package;
import com.lxkplus.mybatisMaker.service.FileCreateService.DDLService;
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
    DDLService ddlService;
    @Value("${mybatis-maker.package.ddl-package}")
    String DDLPackage;
    @Value("${mybatis-maker.package.mybatis-plus-bean-package}")
    String mybatisPlusPackage;
    @Value("${mybatis-maker.mybatis.mybatis-bean-package}")
    String mybatisPackage;
    @Value("${mybatis-maker.mybatis.mapper-package}")
    String mapperPackage;
    @Value("${mybatis-maker.mybatis.xml-package}")
    String xmlPackage;
    @Value("${mybatis-maker.mybatis.jdbc_type}")
    boolean showJdbcType;
    @Value("${mybatis-maker.connect.active_database:null}")
    String activeDatabase;

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

    private String convertTableNameToJavaBeanName(String shameName, String tableName, boolean active) {
        tableName = tableName.replace(" ", "_").replaceAll(Constants.JAVA_NOT_SUPPORT_CHAR, "");
        if (StringUtils.isBlank(tableName)) {
            return "Random" + r.nextInt();
        }
        if (tableName.charAt(0) >= '0' && tableName.charAt(0) <= '9') {
            return "Number" + tableName;
        }
        List<String> trimFirst = mybatisMakerConf.getTrimFirst();
        String tableNameTrim =  tableName;
        for (String head : trimFirst) {
            tableNameTrim = StringUtils.removeStart(tableNameTrim, head);
        }
        for (String tail : mybatisMakerConf.getTrimLast()) {
            tableNameTrim = StringUtils.removeEnd(tableNameTrim, tail);
        }
        if (active) {
            return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableNameTrim);
        }
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, shameName + "_" + tableNameTrim);
    }

    public void fillMessage(TableMessage tableMessage) throws ClassNotFoundException, SQLException {

        // 是否带数据库前缀
        if (tableMessage.getTableSchema().equals(activeDatabase)) {
            tableMessage.setActiveDatabase(true);
        }

        if (tableMessage.isActiveDatabase()) {
            tableMessage.setDatabaseWithTableName(tableMessage.getTableName());
        } else {
            tableMessage.setDatabaseWithTableName(tableMessage.getTableSchema() + "." + tableMessage.getTableName());
        }

        // 进行名称转化
        String tableName = tableMessage.getTableName();
        String tableSchema = tableMessage.getTableSchema();
        String beanName = this.convertTableNameToJavaBeanName(tableSchema, tableName, tableMessage.isActiveDatabase());
        tableMessage.setJavaBeanName(beanName);
        tableMessage.setMapperName(tableMessage.getJavaBeanName() + "Mapper");
        tableMessage.setFullyQualifiedName(mybatisPackage + "." + tableMessage.getJavaBeanName());
        /*
         * package
         */
        tableMessage.setMybatisXmlPackage(new Package(xmlPackage));
        tableMessage.setMybatisPackage(new Package(mybatisPackage));
        tableMessage.setMybatisPlusPackage(new Package(mybatisPlusPackage));
        tableMessage.setMybatisMapperPackage(new Package(mapperPackage));
        tableMessage.setDDLPackage(new Package(DDLPackage));

        /*
         * Path
         */
        Path xmlPath = pathService.getPath(tableMessage.getMybatisXmlPackage(), tableMessage.getMapperName() + ".xml");
        tableMessage.setMybatisXMLPath(xmlPath);
        Path mybatisPath = pathService.getPath(tableMessage.getMybatisPackage(), tableMessage.getJavaBeanName() + ".java");
        tableMessage.setMybatisPath(mybatisPath);
        Path mybatisPlusPath = pathService.getPath(tableMessage.getMybatisPlusPackage(), tableMessage.getJavaBeanName() + ".java");
        tableMessage.setMybatisPlusPath(mybatisPlusPath);
        Path mapperPath = pathService.getPath(tableMessage.getMybatisMapperPackage(), tableMessage.getMapperName() + ".java");
        tableMessage.setMybatisMapperPath(mapperPath);
        Path ddlPath = pathService.getPath(tableMessage.getDDLPackage(), tableMessage.getTableSchema() + "-" + tableMessage.getJavaBeanName() + ".sql");
        tableMessage.setDDLPath(ddlPath);

        tableMessage.setMybatisResultMapId(tableMessage.getJavaBeanName() + "Map");


        // 生成基本表的建表语句
        String DDL = ddlService.getView(tableMessage);
        DDL = DDL.replaceAll("AUTO_INCREMENT=\\d+", "AUTO_INCREMENT=0");
        tableMessage.setDDL(DDL + ";");

        List<ColumnWithJavaStatus> columns = tableMessage.getColumns();
        columns.sort(Comparator.comparing(ColumnWithJavaStatus::getOrdinalPosition));

        // 添加jDBCType映射
        jdbcTypeService.fillJdbcType(tableMessage);

        // 通过配置文件实现类型映射
        Map<String, String> typeMapper = mybatisMakerConf.getTypeMapper();
        for (ColumnWithJavaStatus column : tableMessage.getColumns()) {
            if (simpleNameMap.containsKey(typeMapper.get(column.getJdbcType()))) {
                Type type = simpleNameMap.get(typeMapper.get(column.getJdbcType()));
                column.setJavaType(type);
            } else if (column.getJdbcType() == null) {
                column.setJavaType(String.class);
            } else {
                column.setJavaType(Class.forName(typeMapper.get(column.getJdbcType())));
            }
        }

        // 获取插入表格
        for (ColumnWithJavaStatus column : tableMessage.getColumns()) {
            if (column.getOrdinalPosition() == 1 && "PRI".equals(column.getColumnKey())) {
                tableMessage.setIdColumn(column);
                if (Set.of(JDBCType.valueOf(Types.BIGINT).getName(), JDBCType.valueOf(Types.INTEGER).getName())
                        .contains(tableMessage.getIdColumn().getJdbcType())) {
                    tableMessage.setIdType(IdType.AUTO);
                } else if (Set.of(JDBCType.valueOf(Types.VARCHAR).getName(), JDBCType.CHAR.getName()).contains(tableMessage.getIdColumn().getJdbcType())) {
                    tableMessage.setIdType(IdType.ASSIGN_UUID);
                }
                break;
            }
        }
    }
}
