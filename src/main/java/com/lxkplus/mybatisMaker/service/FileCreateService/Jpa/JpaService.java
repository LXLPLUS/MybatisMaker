package com.lxkplus.mybatisMaker.service.FileCreateService.Jpa;

import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.dto.*;
import com.lxkplus.mybatisMaker.enums.MapperType;
import com.lxkplus.mybatisMaker.enums.TemplateObject;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisXMLService;
import com.lxkplus.mybatisMaker.utils.ColumnSafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Token;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.jdom2.Element;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class JpaService {

    private TrieMap createTrie(TableFlowContext tableFlowContext) {
        Trie.TrieBuilder trieBuilder = Trie.builder();
        trieBuilder.ignoreOverlaps();
        HashMap<String, ColumnWithJavaStatus> map = new HashMap<>();
        for (ColumnWithJavaStatus column : tableFlowContext.getColumns()) {
            String key = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, column.getJavaColumnName());
            map.put(key.toLowerCase(), column);
            trieBuilder.addKeyword(key);
        }
        trieBuilder.addKeyword("And");
        trieBuilder.addKeyword("Or");

        return new TrieMap(trieBuilder.build(), map);
    }
    public void convertToMybatis(TableFlowContext tableFlowContext, JpaRow jpaRow) {
        TrieMap trie = createTrie(tableFlowContext);
        selectAll(tableFlowContext, jpaRow);
        countBy(tableFlowContext, jpaRow, trie);
        findBy(tableFlowContext, jpaRow, trie);
    }

    private static void selectAll(TableFlowContext tableFlowContext,JpaRow jpaRow) {
        if (!StringUtils.equalsAny(jpaRow.getFuncName(), "findAll", "getAll", "selectAll")) {
            return;
        }
        Jpa2MybatisBuilder jpa2MybatisBuilder = new Jpa2MybatisBuilder(jpaRow);
        Element selectAll = new Element("select");
        selectAll.setAttribute("id", jpaRow.getAlias());
        selectAll.setAttribute("resultMap", tableFlowContext.getMybatisResultMapId());
        selectAll.addContent(MybatisXMLService.selectAll(tableFlowContext));
        jpa2MybatisBuilder.setXml(selectAll);

        MethodSpec.Builder mapper = MethodSpec.methodBuilder(jpaRow.getAlias())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(int.class);
        jpa2MybatisBuilder.setMapper(mapper);
        tableFlowContext.getJpa2MybatisBuilders().add(jpa2MybatisBuilder);
    }


    private static SelectBody whereBody(TableFlowContext tableFlowContext, String whereSql, TrieMap trieMap) {
        SelectBody selectBody = new SelectBody();

        Collection<Token> tokenize = trieMap.getTrie().tokenize(whereSql);

        selectBody.getTrims().addAll(tokenize);

        List<String> list = new ArrayList<>(tokenize).stream().map(Token::getFragment).toList();

        if (selectBody.getTrims().size() == 2 && "In".equals(list.get(1)) && selectBody.getTrims().get(0).isMatch()) {
            selectBody.setMapperType(MapperType.LIST);
            ColumnWithJavaStatus column = trieMap.getColumnMap().get(selectBody.getTrims().get(0).getFragment().toLowerCase());
            selectBody.setWhereBy(String.format("""
                    %s in
                        <foreach collection="list" item = "item" open="(" separator = "," close=")">
                        #{item}
                        </foreach>
                    """, column.getSafeColumnName()));
            selectBody.getColumnWithJavaStatusList().add(column);
            selectBody.setSuccess(true);
            return selectBody;
        }


        // Between逻辑
        if (list.size() == 2 && StringUtils.contains(list.get(1), "Between") && tokenize.iterator().next().isMatch()) {
            ArrayList<Token> tokens = new ArrayList<>(tokenize);
            Token history = tokens.get(0);
            Token token = tokens.get(1);
            if ("Between".equals(token.getFragment())) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(history.getFragment().toLowerCase());
                selectBody.setWhereBy(String.format(" %s > #{start} and %s <![CDATA[ < ]]> #{end}",
                        column.getSafeColumnName(),
                        column.getSafeColumnName()));
                selectBody.setSuccess(true);
                ColumnWithJavaStatus clone = column.clone();
                clone.setJavaColumnName("start");
                selectBody.getColumnWithJavaStatusList().add(clone);

                ColumnWithJavaStatus clone2 = column.clone();
                clone2.setJavaColumnName("end");
                selectBody.getColumnWithJavaStatusList().add(clone2);
                return selectBody;
            }
        }

        // 大于小于比较
        if (list.size() == 2
                && StringUtils.containsAny(list.get(1), "GreaterThan", "GreaterThanEqual", "LessThan", "LessThanEqual")
                && tokenize.iterator().next().isMatch()) {
            ArrayList<Token> tokens = new ArrayList<>(tokenize);
            Token history = tokens.get(0);
            Token token = tokens.get(1);
            if ("GreaterThan".equals(token.getFragment()) || "After".equals(token.getFragment())) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(history.getFragment().toLowerCase());
                selectBody.setWhereBy(String.format(" %s > #{%s}",
                        column.getSafeColumnName(),
                        column.getJavaColumnName()));
                selectBody.setSuccess(true);
                selectBody.getColumnWithJavaStatusList().add(column);
                return selectBody;
            }

            else if ("GreaterThanEqual".equals(token.getFragment())) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(token.getFragment().toLowerCase());
                selectBody.setWhereBy(String.format(" %s >= #{%s}",
                        column.getSafeColumnName(),
                        column.getJavaColumnName()));
                selectBody.setSuccess(true);
                selectBody.getColumnWithJavaStatusList().add(column);
                return selectBody;
            }

            else if ("LessThan".equals(token.getFragment()) || "Before".equals(token.getFragment())) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(token.getFragment().toLowerCase());
                selectBody.setWhereBy(String.format(" %s <![CDATA[ < ]]> #{%s}",
                        column.getSafeColumnName(),
                        column.getJavaColumnName()));
                selectBody.setSuccess(true);
                selectBody.getColumnWithJavaStatusList().add(column);
                return selectBody;
            }

            else if ("LessThanEqual".equals(token.getFragment())) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(token.getFragment().toLowerCase());
                selectBody.setWhereBy(String.format(" %s <![CDATA[ <= ]]> #{%s}",
                        column.getSafeColumnName(),
                        column.getJavaColumnName()));

                selectBody.setSuccess(true);
                selectBody.getColumnWithJavaStatusList().add(column);
                return selectBody;
            }
        }
        selectBody.setSuccess(true);
        StringBuilder whereBody = new StringBuilder();
        for (Token token : tokenize) {
            if (token.isMatch() && token.getFragment().equals("And")) {
                whereBody.append(" and ");
            }
            else if (token.isMatch() && token.getFragment().equals("Or")) {
                whereBody.append(" or ");
            }

            else if (token.isMatch()) {
                ColumnWithJavaStatus column = trieMap.getColumnMap().get(token.getFragment().toLowerCase());
                String javaBeanName = column.getJavaColumnName();
                String columnName = column.getColumnName();
                whereBody.append(String.format("%s = #{%s}", ColumnSafeUtils.safeColumn(columnName), javaBeanName));
                selectBody.getColumnWithJavaStatusList().add(column);
            }
            else {
                log.warn("{} {} 解析失败， 字段 {}", tableFlowContext.getJavaBeanName(),
                        whereSql,
                        tokenize.stream().map(x -> x.isMatch() ? x.getFragment() + "(符合)": x.getFragment() + "(不符)")
                                .toList());
                selectBody.setSuccess(false);
                return selectBody;
            }
            selectBody.setWhereBy(whereBody.toString());
        }
        return selectBody;
    }


    private static void findBy(TableFlowContext tableFlowContext, JpaRow jpaRow, TrieMap trieMap) {
        if (!StringUtils.startsWith(jpaRow.getFuncName(), "findBy")) {
            return;
        }

        Jpa2MybatisBuilder jpa2MybatisBuilder = new Jpa2MybatisBuilder(jpaRow);

        String findBy = StringUtils.removeStart(jpaRow.getFuncName(), "findBy");
        SelectBody selectBody = whereBody(tableFlowContext, findBy, trieMap);

        if (!selectBody.isSuccess()) {
            return;
        }

        List<String> list = tableFlowContext.getColumns()
                .stream()
                .map(ColumnWithJavaStatus::getSafeColumnName)
                .toList();
        String join = StringUtils.join(list, ", ");

        Element select = new Element("select");
        select.setAttribute("id", jpaRow.getAlias());
        select.setAttribute("resultType", tableFlowContext.getMybatisResultMapId());
        select.addContent(String.format("""
                                          select %s
                                          from %s
                                          where %s
                                          """,
                join,
                tableFlowContext.getSafeTableName(),
                selectBody.getWhereBy()));

        jpa2MybatisBuilder.setXml(select);

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(List.class, TemplateObject.class);

        MethodSpec.Builder mapper = MethodSpec.methodBuilder(jpaRow.getAlias())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(parameterizedTypeName);

        addParams(selectBody, mapper, selectBody.getMapperType());

        jpa2MybatisBuilder.setMapper(mapper);
        tableFlowContext.getJpa2MybatisBuilders().add(jpa2MybatisBuilder);
    }


    private static void addParams(SelectBody selectBody, MethodSpec.Builder mapper, MapperType mapperType) {
        if (mapperType.equals(MapperType.SINGLE)) {
            addSingleParams(selectBody, mapper);
        } else if (mapperType.equals(MapperType.LIST)) {
            addListParams(selectBody, mapper);
        }
    }
    private static void addListParams(SelectBody selectBody,
                                      MethodSpec.Builder mapper) {
        for (ColumnWithJavaStatus columnWithJavaStatus : selectBody.getColumnWithJavaStatusList()) {
            ParameterSpec.Builder builder
                    = ParameterSpec.builder(ParameterizedTypeName.get(List.class, columnWithJavaStatus.getJavaType()), "list");

            AnnotationSpec annotationBuild = AnnotationSpec.builder(Param.class)
                    .addMember("value", "$S", "list")
                    .build();

            builder.addAnnotation(annotationBuild);
            mapper.addParameter(builder.build());
        }
    }

    private static void addSingleParams(SelectBody selectBody,
                                        MethodSpec.Builder mapper) {
        for (ColumnWithJavaStatus columnWithJavaStatus : selectBody.getColumnWithJavaStatusList()) {
            ParameterSpec.Builder builder = ParameterSpec.builder(columnWithJavaStatus.getJavaType(),
                    columnWithJavaStatus.getJavaColumnName());

            AnnotationSpec annotationBuild = AnnotationSpec.builder(Param.class)
                    .addMember("value", "$S", columnWithJavaStatus.getJavaColumnName())
                    .build();

            builder.addAnnotation(annotationBuild);
            mapper.addParameter(builder.build());
        }
    }

    private static void countBy(TableFlowContext tableFlowContext, JpaRow jpaRow, TrieMap trieMap) {
        if (!StringUtils.startsWith(jpaRow.getFuncName(), "countBy")) {
            return;
        }
        Jpa2MybatisBuilder jpa2MybatisBuilder = new Jpa2MybatisBuilder(jpaRow);

        String countBy = StringUtils.removeStart(jpaRow.getFuncName(), "countBy");
        SelectBody selectBody = whereBody(tableFlowContext, countBy, trieMap);

        if (!selectBody.isSuccess()) {
            return;
        }

        Element countXml = new Element("select");
        countXml.setAttribute("id", jpaRow.getAlias());
        countXml.setAttribute("resultType", int.class.getSimpleName());
        countXml.addContent(String.format("select count(*) from %s where %s",
                tableFlowContext.getSafeTableName(),
                selectBody.getWhereBy()));

        jpa2MybatisBuilder.setXml(countXml);

        MethodSpec.Builder mapper = MethodSpec.methodBuilder(jpaRow.getAlias())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(int.class);

        addParams(selectBody, mapper, selectBody.getMapperType());

        jpa2MybatisBuilder.setMapper(mapper);
        tableFlowContext.getJpa2MybatisBuilders().add(jpa2MybatisBuilder);
    }
}
