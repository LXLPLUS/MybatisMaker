package com.lxkplus.mybatisMaker.service.FileCreateService;

import com.baomidou.mybatisplus.annotation.IdType;
import com.lxkplus.mybatisMaker.conf.MybatisInterFaceConf;
import com.lxkplus.mybatisMaker.dto.TableMessage;
import com.lxkplus.mybatisMaker.enums.Constants;
import com.lxkplus.mybatisMaker.enums.TemplateObject;
import com.lxkplus.mybatisMaker.service.PathService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.javapoet.*;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class MapperService implements FileCreateService {
    @Resource
    MybatisInterFaceConf mybatisInterFaceConf;
    @Resource
    PathService pathService;

    @Override
    public void deleteFile(TableMessage tableMessage) throws IOException {
        Path mybatisMapperPath = tableMessage.getMybatisMapperPath();
        FileUtils.deleteDirectory(mybatisMapperPath.getParent().toFile());
    }

    @Override
    public void createFile(TableMessage table) throws IOException {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(table.getMapperName())
                .addModifiers(Modifier.PUBLIC).addAnnotation(Mapper.class);

        if (mybatisInterFaceConf.isInsert() && Constants.BaseTable.equals(table.getTableType())) {
            MethodSpec.Builder insert = MethodSpec.methodBuilder(Constants.insert);
            insert.addParameter(TemplateObject.class, "entity");
            insert.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            insert.returns(int.class);
            builder.addMethod(insert.build());
        }

        if (mybatisInterFaceConf.isDeleteById()
                && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            MethodSpec.Builder deleteByIdBuilder = MethodSpec.methodBuilder(Constants.deleteById);
            deleteByIdBuilder.returns(int.class).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            addIDBuilder(table, builder, deleteByIdBuilder);
        }

        if (mybatisInterFaceConf.isUpdateById() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            MethodSpec.Builder updateById = MethodSpec.methodBuilder(Constants.updateById);
            updateById.returns(int.class).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            updateById.addParameter(TemplateObject.class, "entity");
            builder.addMethod(updateById.build());
        }

        if (mybatisInterFaceConf.isSelectById() && table.getIdColumn() != null) {
            MethodSpec.Builder selectById = MethodSpec.methodBuilder(Constants.selectById);
            selectById.returns(TemplateObject.class).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            addIDBuilder(table, builder, selectById);
        }

        if (mybatisInterFaceConf.isSelectByIds() && table.getIdColumn() != null) {
            MethodSpec.Builder selectByIds = MethodSpec.methodBuilder(Constants.selectByIds);
            selectByIds.returns(ParameterizedTypeName.get(List.class, TemplateObject.class))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            if (IdType.AUTO.equals(table.getIdType())) {
                selectByIds.addParameter(ParameterizedTypeName.get(List.class, Integer.class), "ids");
            } else {
                selectByIds.addParameter(ParameterizedTypeName.get(List.class, String.class), "ids");
            }
            builder.addMethod(selectByIds.build());
        }

        if (mybatisInterFaceConf.isInsertList() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            MethodSpec.Builder insertList = MethodSpec.methodBuilder(Constants.insertList);
            insertList.returns(int.class).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            insertList.addParameter(ParameterizedTypeName.get(List.class, TemplateObject.class), "lists");
            builder.addMethod(insertList.build());
        }

        if (mybatisInterFaceConf.isDeleteByIds() && table.getIdColumn() != null
                && Constants.BaseTable.equals(table.getTableType())) {
            MethodSpec.Builder deleteByIds = MethodSpec.methodBuilder(Constants.deleteByIds);
            deleteByIds.returns(int.class).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            deleteByIds.addParameter(ParameterizedTypeName.get(List.class, Integer.class), "ids");
            builder.addMethod(deleteByIds.build());
        }

        if (mybatisInterFaceConf.isSelectAll()) {
            MethodSpec.Builder selectAll = MethodSpec.methodBuilder(Constants.selectAll);
            selectAll.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            selectAll.returns(ParameterizedTypeName.get(List.class, TemplateObject.class));
            builder.addMethod(selectAll.build());
        }
        JavaFile javaClassBuilder = JavaFile.builder(table.getMybatisMapperPackage().getPackageName(), builder.build()).build();
        String string = javaClassBuilder.toString();

        string = string.replace(TemplateObject.class.getTypeName(), table.getFullyQualifiedName());
        string = string.replace(TemplateObject.class.getSimpleName(), table.getJavaBeanName());
        pathService.createFile(table.getMybatisMapperPath(), string);
    }

    private void addIDBuilder(TableMessage table, TypeSpec.Builder builder, MethodSpec.Builder selectById) {
        ParameterSpec.Builder id;
        if (IdType.AUTO.equals(table.getIdType())) {
            id = ParameterSpec.builder(Integer.class, "id");
        } else {
            id = ParameterSpec.builder(String.class, "id");
        }
        id.addAnnotation(AnnotationSpec.builder(Param.class).addMember("value", "$S","id").build());
        selectById.addParameter(id.build());
        builder.addMethod(selectById.build());
    }
}
