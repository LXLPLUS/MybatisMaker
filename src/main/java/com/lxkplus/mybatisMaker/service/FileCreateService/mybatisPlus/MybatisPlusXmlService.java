package com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus;

import com.lxkplus.mybatisMaker.conf.GenerateConf;
import com.lxkplus.mybatisMaker.dto.Jpa2MybatisBuilder;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisXMLService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.jdom2.Document;
import org.jdom2.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

@Service
public class MybatisPlusXmlService implements FileCreateService {

    @Resource
    GenerateConf generateConf;

    @Resource
    MybatisXMLService mybatisXMLService;

    @Override
    public boolean needGenerate() {
        return generateConf.isMybatisPlus();
    }

    @Override
    public void createFile(@NotNull TableFlowContext table) throws IOException {
        if (!Files.exists(table.getMybatisXmlPath().getParent())) {
            Files.createDirectories(table.getMybatisXmlPath().getParent());
        }
        Document mybatisDocument = mybatisXMLService.createMybatisDocument(table.getMybatisPlusMapperPackage().getPackageName() + "." + table.getMapperName());
        Element resultMap = mybatisXMLService.getResultElement(table);
        mybatisDocument.getRootElement().addContent(resultMap);

        for (Jpa2MybatisBuilder jpa2MybatisBuilder : table.getJpa2MybatisBuilders()) {
            if (jpa2MybatisBuilder.isMybatisPlus()) {
                mybatisDocument.getRootElement().addContent(jpa2MybatisBuilder.getXml().clone());
            }
        }
        mybatisXMLService.createXML(mybatisDocument, table.getMybatisPlusXmlPath());
    }
}
