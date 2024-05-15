package com.lxkplus.mybatisMaker.service;

import com.google.common.base.CaseFormat;
import com.lxkplus.mybatisMaker.dto.ColumnWithJavaStatus;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.enums.TemplateObject;
import jakarta.annotation.PostConstruct;
import lombok.*;
import org.springframework.javapoet.MethodSpec;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@Service
@Getter
public class LombokService {
    Map<String, Class<?>> map = new LinkedHashMap<>();
    @PostConstruct
    void init() {
        map.put(Data.class.getSimpleName(), Data.class);
        map.put(AllArgsConstructor.class.getSimpleName(), AllArgsConstructor.class);
        map.put(NoArgsConstructor.class.getSimpleName(), NoArgsConstructor.class);
        map.put(Getter.class.getSimpleName(), NoArgsConstructor.class);
        map.put(Setter.class.getSimpleName(), Setter.class);
        map.put(ToString.class.getSimpleName(), ToString.class);
        map.put(EqualsAndHashCode.class.getSimpleName(), EqualsAndHashCode.class);
    }

    public MethodSpec getBuilder(ColumnWithJavaStatus columnWithJavaStatus) {
        String javaColumnName = columnWithJavaStatus.getJavaColumnName();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, javaColumnName));
        if (columnWithJavaStatus.getJavaType().equals(Boolean.class) || columnWithJavaStatus.getJavaType().equals(boolean.class)) {
            builder.setName("is" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, javaColumnName));
        }
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(columnWithJavaStatus.getJavaType());
        builder.addStatement("return this.$N", javaColumnName);
        return builder.build();
    }

    public MethodSpec setBuilder(ColumnWithJavaStatus columnWithJavaStatus) {
        String javaColumnName = columnWithJavaStatus.getJavaColumnName();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, javaColumnName));
        builder.returns(void.class);
        builder.addModifiers(Modifier.PUBLIC);
        builder.addParameter(columnWithJavaStatus.getJavaType(), javaColumnName);
        builder.addStatement("this.$N = that.$N", javaColumnName, javaColumnName);
        return builder.build();
    }


    public MethodSpec equalsBuilder(TableFlowContext tableFlowContext) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("equals");
        builder.addModifiers(Modifier.PUBLIC);
        builder.addAnnotation(Override.class);
        builder.addParameter(Object.class, "object");
        builder.returns(boolean.class);
        builder.addCode("""
                if (this == object) return true;
                if (object == null || getClass() != object.getClass()) return false;
                $T that = ($T) object
                """, TemplateObject.class, TemplateObject.class);
        builder.addCode("return ");
        for (int i = 0; i < tableFlowContext.getColumns().size(); i++) {
            ColumnWithJavaStatus column = tableFlowContext.getColumns().get(i);
            builder.addCode("$T.equals(this.$N, that.$N)", Objects.class, column.getJavaColumnName(), column.getJavaColumnName());
            if (i !=  tableFlowContext.getColumns().size() - 1) {
                builder.addCode(" && ");
            }
        }
        return builder.build();
    }
    public MethodSpec hashCodeBuilder(TableFlowContext tableFlowContext) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("hashCode");
        builder.addModifiers(Modifier.PUBLIC);
        builder.returns(int.class);
        builder.addAnnotation(Override.class);
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        tableFlowContext.getColumns().stream().map(ColumnWithJavaStatus::getJavaColumnName).toList().forEach(sj::add);
        builder.addStatement("return $T.hash" + sj, Objects.class);
        return builder.build();
    }
}
