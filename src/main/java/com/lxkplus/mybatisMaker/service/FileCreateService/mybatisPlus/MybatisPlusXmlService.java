package com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus;

import com.lxkplus.mybatisMaker.conf.GenerateConf;
import com.lxkplus.mybatisMaker.dto.TableFlowContext;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisXMLService;
import jakarta.annotation.Resource;
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
    public boolean generate() {
        return generateConf.isMybatisPlus();
    }

    @Override
    public void createFile(TableFlowContext table) throws IOException {
        if (!Files.exists(table.getMybatisXmlPath().getParent())) {
            Files.createDirectories(table.getMybatisXmlPath().getParent());
        }
        Document mybatisDocument = mybatisXMLService.createMybatisDocument(table.getMybatisPlusMapperPackage().getPackageName() + "." + table.getMapperName());
        Element resultMap = mybatisXMLService.getResultElement(table);
        mybatisDocument.getRootElement().addContent(resultMap);
        mybatisXMLService.createXML(mybatisDocument, table.getMybatisPlusXmlPath());
    }
}
