package com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lxkplus.mybatisMaker.conf.GenerateConf;
import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.conf.MybatisPlusConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.LombokService;
import com.lxkplus.mybatisMaker.service.PathService;
import com.lxkplus.mybatisMaker.service.TemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.TypeSpec;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Service
@Slf4j
public class MybatisPlusEntityService implements FileCreateService {

    @Resource
    MybatisPlusConf mybatisPlusConf;
    @Resource
    PathService pathService;
    @Resource
    TemplateService templateService;
    @Resource
    LombokService lombokService;
    @Resource
    MybatisMakerConf mybatisMakerConf;

    @Resource
    GenerateConf generateConf;
    @Override
    public boolean needGenerate() {
        return generateConf.isMybatisPlus();
    }

    @Override
    public void createFile(@NotNull TableFlowContext table) throws IOException {

        TypeSpec.Builder builder = TypeSpec.classBuilder(table.getJavaBeanName())
                .addModifiers(Modifier.PUBLIC);

        for (String annotation : mybatisMakerConf.getLombok()) {
            if (lombokService.getMap().containsKey(annotation)) {
                builder.addAnnotation(lombokService.getMap().get(annotation));
            }
        }


        // 操作tableName
        AnnotationSpec.Builder tableHeader = AnnotationSpec.builder(TableName.class);
        tableHeader.addMember("value", "$S", table.getTableName());
        if (!table.isActiveDatabase()) {
            tableHeader.addMember("schema", "$S", table.getTableSchema());
        }
        builder.addAnnotation(tableHeader.build());

        // 添加序列化逻辑
        if (mybatisMakerConf.isSerializable()) {
            builder.addSuperinterface(Serializable.class);
            FieldSpec.Builder serialVersionUID = FieldSpec.builder(long.class,
                    "serialVersionUID",
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            serialVersionUID.addAnnotation(Serial.class);
            serialVersionUID.initializer("1L");
            AnnotationSpec.Builder tableFiled = AnnotationSpec.builder(TableField.class)
                    .addMember("exist", "false");
            serialVersionUID.addAnnotation(tableFiled.build());
            builder.addField(serialVersionUID.build());
        }

        // 操作行
        for (ColumnWithJavaStatus column : table.getColumns()) {
            FieldSpec.Builder columnBuilder = FieldSpec.builder(column.getJavaType(), column.getJavaColumnName(), Modifier.PRIVATE);

            if (mybatisMakerConf.isJavaDocExist()) {
                String format = templateService.replace(mybatisMakerConf.getColumnDocFormat(), column);
                columnBuilder.addJavadoc(format);
            }

            // 操作tableId和TableField
            if (Objects.equals(table.getIdColumn(), column)) {
                AnnotationSpec.Builder columnAnnotation = AnnotationSpec.builder(TableId.class);
                if (!table.isActiveDatabase()) {
                    columnAnnotation.addMember("value", "$S", column.getColumnName());
                }
                columnAnnotation.addMember("type", "$T.$L", IdType.class, table.getIdType());
                columnBuilder.addAnnotation(columnAnnotation.build());
            } else {
                if (!table.isActiveDatabase() || !column.isConvertMysql2JavaStatus()) {
                    AnnotationSpec.Builder columnAnnotation = AnnotationSpec.builder(TableField.class)
                            .addMember("value", "$S", column.getColumnName());
                    columnBuilder.addAnnotation(columnAnnotation.build());
                }
            }
            builder.addField(columnBuilder.build());
        }

        JavaFile javaClassBuilder = JavaFile.builder(mybatisPlusConf.getMybatisPlusBeanPackage(), builder.build()).build();
        pathService.createFile(table.getMybatisPlusEntityPath(), javaClassBuilder.toString());
    }
}
