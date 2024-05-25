package com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lxkplus.mybatisMaker.conf.GenerateConf;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.enums.TemplateObject;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.PathService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;

@Service
public class MybatisPlusMapperService implements FileCreateService {

    @Resource
    GenerateConf generateConf;

    @Resource
    PathService pathService;
    @Override
    public boolean generate() {
        return generateConf.isMybatisPlus();
    }

    @Override
    public void createFile(@NotNull TableFlowContext table) throws IOException {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(table.getMapperName())
                .addModifiers(Modifier.PUBLIC).addAnnotation(Mapper.class);

        builder.addSuperinterface(ParameterizedTypeName.get(BaseMapper.class, TemplateObject.class));

        JavaFile javaClassBuilder = JavaFile.builder(table.getMybatisPlusMapperPackage().getPackageName(), builder.build()).build();
        String string = javaClassBuilder.toString()
                .replace(TemplateObject.class.getTypeName(), table.getFullyQualifiedName())
                .replace(TemplateObject.class.getSimpleName(), table.getJavaBeanName());
        pathService.createFile(table.getMybatisPlusMapperPath(), string);
    }
}
