package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.lxkplus.mybatisMaker.conf.MybatisMakerConf;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.enums.TemplateObject;
import com.lxkplus.mybatisMaker.service.LombokService;
import com.lxkplus.mybatisMaker.service.PathService;
import com.lxkplus.mybatisMaker.service.TemplateService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.TypeSpec;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

@Service
@Slf4j
public class MybatisService implements FileCreateService {
    @Resource
    PathService pathService;
    @Value("${mybatis-maker.column_doc_format}")
    String docTemplate;
    @Resource
    TemplateService templateService;
    @Resource
    MybatisMakerConf mybatisMakerConf;
    String dateTimeFormat = "yyyy-MM-dd-HH-mm-ss";
    @Value("${mybatis-maker.serializable}")
    boolean serializable;
    @Resource
    LombokService lombokService;

    @Override
    public void createFile(TableMessage table) throws IOException {
        if (table.getMybatisPackage() == null) {
            return;
        }
        TypeSpec.Builder builder = TypeSpec.classBuilder(table.getJavaBeanName())
                .addModifiers(Modifier.PUBLIC);

        if (mybatisMakerConf.isUseLombok()) {
            for (String annotation : mybatisMakerConf.getLombok()) {
                if (lombokService.getMap().containsKey(annotation)) {
                    builder.addAnnotation(lombokService.getMap().get(annotation));
                }
            }
        }


        if (serializable) {
            builder.addSuperinterface(Serializable.class);
            FieldSpec.Builder serialVersionUID = FieldSpec.builder(long.class,
                    "serialVersionUID",
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            serialVersionUID.addAnnotation(Serial.class);
            serialVersionUID.initializer("1L");
            builder.addField(serialVersionUID.build());
        }

        if (mybatisMakerConf.isJavaDocExist()) {
            builder.addJavadoc(table.getDatabaseWithTableName());
        }

        if (mybatisMakerConf.isSwagger3Exist()) {
            AnnotationSpec.Builder head = AnnotationSpec.builder(Schema.class);
            head.addMember("title", "$S", table.getTableName());
            if (table.getTableComment() != null) {
                head.addMember("description", "$S", table.getTableComment());
            }
        }
        for (ColumnWithJavaStatus column : table.getColumns()) {
            FieldSpec.Builder columnBuilder = FieldSpec.builder(column.getJavaType(), column.getJavaColumnName(), Modifier.PRIVATE);
            if (mybatisMakerConf.isJavaDocExist()) {
                String format = templateService.replace(docTemplate, column);
                columnBuilder.addJavadoc(format);
            }

            // swagger注解
            if (mybatisMakerConf.isSwagger3Exist()) {
                AnnotationSpec.Builder swagger = AnnotationSpec.builder(Schema.class);
                swagger.addMember("title", "$S", column.getColumnName());
                if (column.getCharacterMaximumLength() != null) {
                    swagger.addMember("maxLength", "$L", Long.toString(column.getCharacterMaximumLength()));
                }
                swagger.addMember("type", "$S", column.getColumnType());
                if (column.getColumnComment() != null) {
                    swagger.addMember("description", "$S", column.getColumnComment());
                }
                swagger.addMember("nullable", "$L", column.getIsNullable());
                if (column.getColumnDefault() != null) {
                    swagger.addMember("defaultValue", "$S", column.getColumnDefault());
                }
                columnBuilder.addAnnotation(swagger.build());
            }
            if (column.isCheckDateTime()) {
                AnnotationSpec.Builder pattern =
                        AnnotationSpec.builder(DateTimeFormat.class).addMember("pattern", "$S",dateTimeFormat);
                columnBuilder.addAnnotation(pattern.build());
            }
            builder.addField(columnBuilder.build());
        }

        if (!mybatisMakerConf.isUseLombok()) {
            for (ColumnWithJavaStatus column : table.getColumns()) {
                builder.addMethod(lombokService.getBuilder(column));
                builder.addMethod(lombokService.setBuilder(column));
            }

            builder.addMethod(lombokService.hashCodeBuilder(table));
            builder.addMethod(lombokService.equalsBuilder(table));
        }

        JavaFile javaClassBuilder = JavaFile.builder(table.getMybatisPackage().getPackageName(), builder.build()).build();
        String string = javaClassBuilder.toString()
                .replace(TemplateObject.class.getTypeName(), table.getFullyQualifiedName())
                .replace(TemplateObject.class.getSimpleName(), table.getJavaBeanName());
        pathService.createFile(table.getMybatisPath(), string);
    }
}
