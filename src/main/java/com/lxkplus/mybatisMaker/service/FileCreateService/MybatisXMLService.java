package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.conf.MybatisInterFaceConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class MybatisXMLService implements FileCreateService {

    @Resource
    MybatisInterFaceConf mybatisInterFaceConf;

    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

    @Value("${mybatis-maker.mybatis.jdbc_type}")
    boolean showJdbcType;

    @PostConstruct
    void init() {
        xmlOutputter.getFormat().setIndent("\t");
    }

    public Document createMybatisDocument(String namespace) {
        Document document = new Document();
        document.setProperty("version", "1.0");
        document.setProperty("encoding", "UTF-8");

        document.setDocType(new DocType("mapper",
                "-//mybatis.org//DTD Mapper 3.0//EN",
                "http://mybatis.org/dtd/mybatis-3-mapper.dtd"));
        Element mapper = new Element("mapper");
        mapper.setAttribute("namespace", namespace);
        document.setRootElement(mapper);
        return document;
    }


    public void createXML(Document document, Path path) throws IOException {
        String s = xmlOutputter.outputString(document);
        StringJoiner sj = getStringJoiner(s);
        s = sj.toString()
                .replace("</select>", "\n\t</select>\n")
                .replace("</update>", "\n\t</update>\n")
                .replace("</insert>", "\n\t</insert>\n")
                .replace("</delete>", "\n\t</delete>\n");
        String[] split = s.split("\n");
        for (int i = 0; i < split.length; i++) {
            if (!StringUtils.isBlank(split[i]) && !StringUtils.startsWith(split[i].strip(), "<")) {
                split[i] = "\t\t" + split[i].replace("&lt;", "<")
                        .replace("&gt;", ">");
            }
        }
        Files.writeString(path, StringUtils.join(split, "\n"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @NotNull
    private static StringJoiner getStringJoiner(String s) {
        String[] split = s.split("\n");
        StringJoiner sj = new StringJoiner("\n");
        for (String str : split) {
            if (str.strip().startsWith("<insert")
                    || str.strip().startsWith("<select")
                    || str.strip().startsWith("<update")
                    || str.strip().startsWith("<delete")) {
                String[] split1 = str.split("(?<=>)", 2);
                sj.add(split1[0]);
                sj.add(split1[1]);
            }
            else {
                sj.add(str);
            }
        }
        return sj;
    }


    @Override
    public void createFile(TableMessage table) throws IOException {
        if (!Files.exists(table.getMybatisXMLPath().getParent())) {
            Files.createDirectories(table.getMybatisXMLPath().getParent());
        }
        Document mybatisDocument = this.createMybatisDocument(table.getMybatisMapperPackage().getPackageName() + "." + table.getMapperName());
        Element resultMap = getResultElement(table);
        mybatisDocument.getRootElement().addContent(resultMap);
        if (mybatisInterFaceConf.isInsert()) {
            Element select = new Element("insert");
            select.setAttribute("id", "insert");
            select.addContent(insert(table));
            mybatisDocument.getRootElement().addContent(select);
        }
        if (mybatisInterFaceConf.isDeleteById() && table.getIdColumn() != null) {
            Element select = new Element("delete");
            select.setAttribute("id", "deleteById");
            select.addContent(deleteByID(table));
            mybatisDocument.getRootElement().addContent(select);
        }
        if (mybatisInterFaceConf.isUpdateById() && table.getIdColumn() != null) {
            Element update = new Element("update");
            update.setAttribute("id", "updateById");
            update.addContent(updateById(table));
            mybatisDocument.getRootElement().addContent(update);
        }
        if (mybatisInterFaceConf.isSelectById() && table.getIdColumn() != null) {
            Element selectById = new Element("select");
            selectById.setAttribute("id", "selectById");
            selectById.setAttribute("resultMap", table.getMybatisResultMapId());
            selectById.addContent(selectById(table));
            mybatisDocument.getRootElement().addContent(selectById);
        }
        if (mybatisInterFaceConf.isSelectByIds() && table.getIdColumn() != null) {
            Element selectByIds = new Element("select");
            selectByIds.setAttribute("id", "selectByIds");
            selectByIds.setAttribute("resultMap", table.getMybatisResultMapId());
            selectByIds.addContent(selectByIds(table));
            mybatisDocument.getRootElement().addContent(selectByIds);
        }
        if (mybatisInterFaceConf.isDeleteByIds() && table.getIdColumn() != null) {
            Element deleteByIds = new Element("delete");
            deleteByIds.setAttribute("id", "deleteByIds");
            deleteByIds.addContent(deleteByIds(table));
            mybatisDocument.getRootElement().addContent(deleteByIds);

        }
        if (mybatisInterFaceConf.isInsertList() &&table.getIdColumn() != null) {
            Element insertList = new Element("insert");
            insertList.setAttribute("id", "insertList");
            insertList.addContent(selectList(table));
            mybatisDocument.getRootElement().addContent(insertList);
        }
        createXML(mybatisDocument, table.getMybatisXMLPath());
    }

    @NotNull
    private Element getResultElement(TableMessage table) {
        Element resultMap = new Element("resultMap");
        resultMap.setAttribute("id", table.getMybatisResultMapId());
        resultMap.setAttribute("type", table.getFullyQualifiedName());

        /*
         * id在第一位
         */
        ColumnWithJavaStatus idColumn = table.getIdColumn();
        if (idColumn != null) {
            Element idResult = new Element("id");
            idResult.setAttribute("property", idColumn.getJavaColumnName());
            idResult.setAttribute("column", idColumn.getColumnName());
            if (showJdbcType) {
                idResult.setAttribute("jdbcType", idColumn.getJdbcType());
            }
            resultMap.addContent(idResult);
        }
        for (ColumnWithJavaStatus column : table.getColumns()) {
            if (table.getIdColumn() == column) {
                continue;
            }
            Element result = new Element("result");
            result.setAttribute("property", column.getJavaColumnName());
            result.setAttribute("column", column.getColumnName());
            if (showJdbcType) {
                result.setAttribute("jdbcType", column.getJdbcType());
            }
            resultMap.addContent(result);
        }
        return resultMap;
    }

    private static String insert(TableMessage tableMessage) {
        List<String> collect = tableMessage.getColumns().stream().map(ColumnWithJavaStatus::getColumnName).map(x -> "`" + x + "`").toList();
        List<String> values = tableMessage.getColumns().stream().map(ColumnWithJavaStatus::getJavaColumnName).map(x -> "#{" + x + "}").toList();
        return String.format("""
                insert into %s
                \t(%s)
                \t\tvalues
                \t(%s);
                """,
                tableMessage.getDatabaseWithTableName(),
                StringUtils.join(collect, ", "),
                StringUtils.join(values, ", ")
        );
    }

    private static String deleteByID(TableMessage tableMessage) {
        return String.format("delete from %s \n\twhere `%s` = #{%s};",
                tableMessage.getDatabaseWithTableName(),
                tableMessage.getIdColumn().getColumnName(),
                tableMessage.getIdColumn().getJavaColumnName());
    }

    private static String updateById(TableMessage tableMessage) {
        List<ColumnWithJavaStatus> columns = tableMessage.getColumns();
        ArrayList<ColumnWithJavaStatus> columnWithJavaStatuses = new ArrayList<>(columns);
        columnWithJavaStatuses.removeIf(x -> x == tableMessage.getIdColumn());
        List<String> list = columnWithJavaStatuses.stream().map(x -> String.format("`%s` = #{%s}", x.getColumnName(), x.getJavaColumnName())).toList();
        String join = StringUtils.join(list, ", ");
        return String.format("update %s set %s \n\twhere `%s` = #{%s};",
                tableMessage.getDatabaseWithTableName(),
                join,
                tableMessage.getIdColumn().getColumnName(),
                tableMessage.getIdColumn().getJavaColumnName());
    }

    public static String selectById(TableMessage tableMessage) {
        List<String> list = tableMessage.getColumns().stream().map(x -> "`" + x.getColumnName() + "`").toList();

        return String.format("select %s from %s \n\t\twhere `%s` = #{%s}",
                StringUtils.join(list, ", "),
                tableMessage.getDatabaseWithTableName(),
                tableMessage.getIdColumn().getColumnName(),
                tableMessage.getIdColumn().getJavaColumnName());
    }

    public static String selectByIds(TableMessage tableMessage) {
        List<String> list = tableMessage.getColumns().stream().map(x -> "`" + x.getColumnName() + "`").toList();
        return String.format("""
                    select %s from %s
                        where `%s` in
                        <foreach collection="list" item = "item" open="(" separator = "," close=")">
                        #{item}
                        </foreach>
                """, StringUtils.join(list, ", "),
                tableMessage.getDatabaseWithTableName(),
                tableMessage.getIdColumn().getColumnName());
    }

    private static String deleteByIds(TableMessage tableMessage) {
        return String.format("""
                        delete from %s
                            where `%s` in
                            <foreach collection="list" item = "item" open="(" separator = "," close=")">
                            #{item}
                            </foreach>
                        """,
                tableMessage.getDatabaseWithTableName(),
                tableMessage.getIdColumn().getColumnName());
    }

    private static String selectList(TableMessage tableMessage) {
        List<String> collect = tableMessage.getColumns().stream()
                .map(ColumnWithJavaStatus::getColumnName)
                .map(x -> "`" + x + "`")
                .toList();
        List<String> values = tableMessage.getColumns().stream()
                .map(ColumnWithJavaStatus::getJavaColumnName)
                .map(x -> "#{item." + x + "}")
                .toList();
        return String.format("""
               insert into %s
                    (%s)
                    values
                    <foreach collection="list" item = "item" separator = ",">
                    (%s)
                    </foreach>
                """,
                tableMessage.getDatabaseWithTableName(),
                StringUtils.join(collect, ", "),
                StringUtils.join(values, ", ")
        );
    }
}
