package com.lxkplus.mybatisMaker.service.FileCreateService.Jpa;

import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.Jpa2MybatisBuilder;
import com.lxkplus.mybatisMaker.dto.SelectBody;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
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
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.HashMap;

@Service
@Slf4j
public class JpaService {
    public void convertToMybatis(TableFlowContext tableFlowContext, String query) {
        selectAll(tableFlowContext, query);
        countBy(tableFlowContext, query);
    }

    private static void selectAll(TableFlowContext tableFlowContext, String query) {
        if (!StringUtils.equalsAny(query, "findAll", "getAll")) {
            return;
        }
        Jpa2MybatisBuilder jpa2MybatisBuilder = new Jpa2MybatisBuilder();
        Element selectAll = new Element("select");
        selectAll.setAttribute("id", query);
        selectAll.setAttribute("resultMap", tableFlowContext.getMybatisResultMapId());
        selectAll.addContent(MybatisXMLService.selectAll(tableFlowContext));
        jpa2MybatisBuilder.setXml(selectAll);

        MethodSpec.Builder mapper = MethodSpec.methodBuilder(query)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(int.class);
        jpa2MybatisBuilder.setMapper(mapper);
        tableFlowContext.getJpa2MybatisBuilders().add(jpa2MybatisBuilder);
    }


    private static SelectBody whereBody(TableFlowContext tableFlowContext, String query) {
        SelectBody selectBody = new SelectBody();
        Trie.TrieBuilder trieBuilder = Trie.builder();
        trieBuilder.ignoreOverlaps();
        HashMap<String, ColumnWithJavaStatus> map = new HashMap<>();
        for (ColumnWithJavaStatus column : tableFlowContext.getColumns()) {
            String key = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, column.getJavaColumnName());
            map.put(key, column);
            trieBuilder.addKeyword(key);
        }
        trieBuilder.addKeyword("And");
        trieBuilder.addKeyword("Or");

        Trie build = trieBuilder.build();
        Collection<Token> tokenize = build.tokenize(query);
        StringBuilder whereBody = new StringBuilder();

        selectBody.getTrims().addAll(tokenize);
        selectBody.setSuccess(true);
        for (Token token : tokenize) {
            if (token.isMatch() && token.getFragment().equals("And")) {
                whereBody.append(" and ");
            } else if (token.isMatch() && token.getFragment().equals("Or")) {
                whereBody.append(" or ");
            } else if (token.isMatch()) {
                ColumnWithJavaStatus column = map.get(token.getFragment());
                String javaBeanName = column.getJavaColumnName();
                String columnName = column.getColumnName();
                whereBody.append(String.format("%s = #{%s}", ColumnSafeUtils.safeColumn(columnName), javaBeanName));
                selectBody.getColumnWithJavaStatusList().add(column);
            } else {
                log.warn("{} {} 解析失败， 字段 {}", tableFlowContext.getJavaBeanName(),
                        query,
                        tokenize.stream().map(x -> x.isMatch() ? x.getFragment() + "(符合)": x.getFragment() + "(不符)")
                                .toList());
                selectBody.setSuccess(false);
                return selectBody;
            }
            selectBody.setWhereBy(whereBody.toString());
        }
        return selectBody;
    }
    private static void countBy(TableFlowContext tableFlowContext, String query) {
        if (!StringUtils.startsWith(query, "countBy")) {
            return;
        }
        Jpa2MybatisBuilder jpa2MybatisBuilder = new Jpa2MybatisBuilder();
        String countBy = StringUtils.removeStart(query, "countBy");
        SelectBody selectBody = whereBody(tableFlowContext, countBy);

        if (!selectBody.isSuccess()) {
            return;
        }

        Element countXml = new Element("select");
        countXml.setAttribute("id", query);
        countXml.setAttribute("resultType", "int");
        countXml.addContent(String.format("select count(*) from %s where %s",
                tableFlowContext.getSafeTableName(),
                selectBody.getWhereBy()));

        jpa2MybatisBuilder.setXml(countXml);

        MethodSpec.Builder mapper = MethodSpec.methodBuilder(query)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(int.class);

        for (ColumnWithJavaStatus columnWithJavaStatus : selectBody.getColumnWithJavaStatusList()) {
            ParameterSpec.Builder builder = ParameterSpec.builder(columnWithJavaStatus.getJavaType(),
                    columnWithJavaStatus.getJavaColumnName());

            AnnotationSpec annotationBuild = AnnotationSpec.builder(Param.class)
                    .addMember("value", "$S", columnWithJavaStatus.getJavaColumnName())
                    .build();

            builder.addAnnotation(annotationBuild);
            mapper.addParameter(builder.build());
        }

        jpa2MybatisBuilder.setMapper(mapper);
        tableFlowContext.getJpa2MybatisBuilders().add(jpa2MybatisBuilder);
    }
}
