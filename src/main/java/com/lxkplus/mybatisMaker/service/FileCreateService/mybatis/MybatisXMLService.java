package com.lxkplus.mybatisMaker.service.FileCreateService.mybatis;

import com.lxkplus.mybatisMaker.conf.GenerateConf;
import com.lxkplus.mybatisMaker.conf.MybatisInterFaceConf;
import com.lxkplus.mybatisMaker.conf.MybatisMakerMybatisConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.Jpa2MybatisBuilder;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.PathService;
import com.lxkplus.mybatisMaker.utils.ColumnSafeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

@Service
@Slf4j
public class MybatisXMLService implements FileCreateService {

    @Resource
    MybatisInterFaceConf mybatisInterFaceConf;

    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

    @Resource
    MybatisMakerMybatisConf mybatisMakerMybatisConf;

    @Resource
    GenerateConf generateConf;

    @Resource
    PathService pathService;

    Trie build;

    @PostConstruct
    void init() {
        xmlOutputter.getFormat().setIndent("\t");
        Trie.TrieBuilder trieBuilder = Trie.builder();
        trieBuilder.ignoreOverlaps();
        trieBuilder.addKeyword("</select>");
        trieBuilder.addKeyword("</update>");
        trieBuilder.addKeyword("</insert>");
        trieBuilder.addKeyword("</delete>");
        build = trieBuilder.build();
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
        Collection<Token> tokenize = build.tokenize(sj.toString());
        StringBuilder stringBuilder = new StringBuilder();
        for (Token token : tokenize) {
            if (token.isMatch()) {
                stringBuilder.append("\n\t");
                stringBuilder.append(token.getFragment());
                stringBuilder.append("\n");
            } else {
                stringBuilder.append(token.getFragment());
            }
        }
        String[] split = stringBuilder.toString().split("\n");
        for (int i = 0; i < split.length; i++) {
            if (!StringUtils.isBlank(split[i]) && !StringUtils.startsWith(split[i].strip(), "<")) {
                split[i] = "\t\t" + split[i].replace("&lt;", "<")
                        .replace("&gt;", ">");
            }
        }

        if (!Files.isDirectory(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        pathService.createFile(path, StringUtils.join(split, "\n"));
    }

    @NotNull
    private static StringJoiner getStringJoiner(String s) {
        String[] split = s.split("\n");
        StringJoiner sj = new StringJoiner("\n");
        for (String str : split) {
            if (StringUtils.startsWithAny(str.strip(), "<insert", "<select", "<update", "<delete"))
            {
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
    public boolean needGenerate() {
        return generateConf.isMybatis();
    }

    @Override
    public void createFile(TableFlowContext table) throws IOException {
        if (!Files.exists(table.getMybatisXmlPath().getParent())) {
            Files.createDirectories(table.getMybatisXmlPath().getParent());
        }
        Document mybatisDocument = this.createMybatisDocument(table.getMybatisMapperPackage().getPackageName() + "." + table.getMapperName());
        Element resultMap = getResultElement(table);
        mybatisDocument.getRootElement().addContent(resultMap);
        if (mybatisInterFaceConf.isInsert()
                && Constants.BaseTable.equals(table.getTableType())) {
            Element select = new Element("insert");
            select.setAttribute("id", Constants.insert);
            select.addContent(insert(table));
            mybatisDocument.getRootElement().addContent(select);
        }
        if (mybatisInterFaceConf.isInsertList() &&table.getIdColumn() != null && Constants.BaseTable.equals(table.getTableType())) {
            Element insertList = new Element("insert");
            insertList.setAttribute("id", Constants.insertList);
            insertList.addContent(insertList(table));
            mybatisDocument.getRootElement().addContent(insertList);
        }
        if (mybatisInterFaceConf.isDeleteById() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            Element select = new Element("delete");
            select.setAttribute("id", Constants.deleteById);
            select.addContent(deleteByID(table));
            mybatisDocument.getRootElement().addContent(select);
        }
        if (mybatisInterFaceConf.isDeleteByIds() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            Element deleteByIds = new Element("delete");
            deleteByIds.setAttribute("id", Constants.deleteByIds);
            deleteByIds.addContent(deleteByIds(table));
            mybatisDocument.getRootElement().addContent(deleteByIds);
        }
        if (mybatisInterFaceConf.isUpdateById() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType()) && table.getColumns().size() > 1) {
            Element update = new Element("update");
            update.setAttribute("id", Constants.updateById);
            update.addContent(updateById(table));
            mybatisDocument.getRootElement().addContent(update);
        }
        if (mybatisInterFaceConf.isSelectById() && table.getIdColumn() != null) {
            Element selectById = new Element("select");
            selectById.setAttribute("id", Constants.selectById);
            selectById.setAttribute("resultMap", table.getMybatisResultMapId());
            selectById.addContent(selectById(table));
            mybatisDocument.getRootElement().addContent(selectById);
        }

        if (mybatisInterFaceConf.isSelectAll()) {
            Element selectAll = new Element("select");
            selectAll.setAttribute("id", Constants.selectAll);
            selectAll.setAttribute("resultMap", table.getMybatisResultMapId());
            selectAll.addContent(selectAll(table));
            mybatisDocument.getRootElement().addContent(selectAll);
        }
        if (mybatisInterFaceConf.isSelectByIds() && table.getIdColumn() != null) {
            Element selectByIds = new Element("select");
            selectByIds.setAttribute("id", Constants.selectByIds);
            selectByIds.setAttribute("resultMap", table.getMybatisResultMapId());
            selectByIds.addContent(selectByIds(table));
            mybatisDocument.getRootElement().addContent(selectByIds);
        }

        for (Jpa2MybatisBuilder jpa2MybatisBuilder : table.getJpa2MybatisBuilders()) {
            if (jpa2MybatisBuilder.isMybatis()) {
                Element xml = jpa2MybatisBuilder.getXml();
                mybatisDocument.getRootElement().addContent(xml);
            }
        }
        createXML(mybatisDocument, table.getMybatisXmlPath());
    }

    @NotNull
    public Element getResultElement(TableFlowContext table) {
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
            if (mybatisMakerMybatisConf.isJdbcType()) {
                idResult.setAttribute("jdbcType", idColumn.getJdbcType().getName());
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
            if (mybatisMakerMybatisConf.isJdbcType()) {
                result.setAttribute("jdbcType", column.getJdbcType().getName());
            }
            resultMap.addContent(result);
        }
        return resultMap;
    }

    private static String insert(TableFlowContext tableFlowContext) {
        List<String> collect = tableFlowContext.getColumns().stream()
                .filter(x -> !tableFlowContext.getDateTimeAutoColumns().contains(x))
                .map(ColumnWithJavaStatus::getColumnName)
                .map(ColumnSafeUtils::safeColumn)
                .toList();
        List<String> values = tableFlowContext.getColumns()
                .stream()
                .filter(x -> !tableFlowContext.getDateTimeAutoColumns().contains(x))
                .map(ColumnWithJavaStatus::getJavaColumnName)
                .map(x -> "#{" + x + "}").toList();
        return String.format("""
                insert into %s
                \t(%s)
                \t\tvalues (%s);
                """,
                tableFlowContext.getSafeTableName(),
                StringUtils.join(collect, ", "),
                StringUtils.join(values, ", ")
        );
    }

    private static String deleteByID(TableFlowContext tableFlowContext) {
        return String.format("delete from %s \n\twhere %s = #{%s} ",
                tableFlowContext.getSafeTableName(),
                ColumnSafeUtils.safeColumn(tableFlowContext.getIdColumn().getColumnName()),
                tableFlowContext.getIdColumn().getJavaColumnName());
    }

    private static String updateById(TableFlowContext tableFlowContext) {
        List<ColumnWithJavaStatus> columns = tableFlowContext.getColumns();
        ArrayList<ColumnWithJavaStatus> columnWithJavaStatuses = new ArrayList<>(columns);
        columnWithJavaStatuses.removeIf(x -> x == tableFlowContext.getIdColumn());
        List<String> list = columnWithJavaStatuses.stream()
                .filter(x -> !tableFlowContext.getDateTimeAutoColumns().contains(x))
                .map(x -> String.format("%s = #{%s}", ColumnSafeUtils.safeColumn(x.getColumnName()), x.getJavaColumnName()))
                .toList();
        String join = StringUtils.join(list, ", ");
        return String.format("update %s set %s \n\twhere %s = #{%s} ",
                tableFlowContext.getSafeTableName(),
                join,
                ColumnSafeUtils.safeColumn(tableFlowContext.getIdColumn().getColumnName()),
                tableFlowContext.getIdColumn().getJavaColumnName());
    }

    public static String selectById(TableFlowContext tableFlowContext) {
        List<String> list = tableFlowContext.getColumns().stream()
                .map(x -> ColumnSafeUtils.safeColumn(x.getColumnName()))
                .toList();

        return String.format("select %s from %s \n\t\twhere %s = #{%s} ",
                StringUtils.join(list, ", "),
                tableFlowContext.getSafeTableName(),
                ColumnSafeUtils.safeColumn(tableFlowContext.getIdColumn().getColumnName()),
                tableFlowContext.getIdColumn().getJavaColumnName());
    }


    public static String selectAll(TableFlowContext tableFlowContext) {
        List<String> list = tableFlowContext.getColumns().stream()
                .map(x -> ColumnSafeUtils.safeColumn(x.getColumnName()))
                .toList();

        return String.format("select %s from %s ",
                StringUtils.join(list, ", "),
                tableFlowContext.getSafeTableName());

    }
    public static String selectByIds(TableFlowContext tableFlowContext) {
        List<String> list = tableFlowContext.getColumns().stream().map(x -> "`" + x.getColumnName() + "`").toList();
        return String.format("""
                    select %s from %s
                        where %s in
                        <foreach collection="list" item = "item" open="(" separator = "," close=")">
                        #{item}
                        </foreach>
                """, StringUtils.join(list, ", "),
                tableFlowContext.getSafeTableName(),
                ColumnSafeUtils.safeColumn(tableFlowContext.getIdColumn().getColumnName()));
    }

    private static String deleteByIds(TableFlowContext tableFlowContext) {
        return String.format("""
                        delete from %s
                            where %s in
                            <foreach collection="list" item = "item" open="(" separator = "," close=")">
                            #{item}
                            </foreach>
                        """,
                tableFlowContext.getSafeTableName(),
                ColumnSafeUtils.safeColumn(tableFlowContext.getIdColumn().getColumnName()));
    }

    private static String insertList(TableFlowContext tableFlowContext) {
        List<String> collect = tableFlowContext.getColumns().stream()
                .filter(x -> !tableFlowContext.getDateTimeAutoColumns().contains(x))
                .map(ColumnWithJavaStatus::getColumnName)
                .map(ColumnSafeUtils::safeColumn)
                .toList();
        List<String> values = tableFlowContext.getColumns().stream()
                .filter(x -> !tableFlowContext.getDateTimeAutoColumns().contains(x))
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
                tableFlowContext.getSafeTableName(),
                StringUtils.join(collect, ", "),
                StringUtils.join(values, ", ")
        );
    }
}
