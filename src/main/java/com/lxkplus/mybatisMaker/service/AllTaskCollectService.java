package com.lxkplus.mybatisMaker.service;

import com.lxkplus.mybatisMaker.service.FileCreateService.DDLService;
import com.lxkplus.mybatisMaker.service.FileCreateService.FileCreateService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisEntityService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisMapperService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatis.MybatisXMLService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusEntityService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusMapperService;
import com.lxkplus.mybatisMaker.service.FileCreateService.mybatisPlus.MybatisPlusXmlService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AllTaskCollectService {

    @Resource
    MybatisEntityService mybatisEntityService;
    @Resource
    DDLService ddlService;
    @Resource
    MybatisPlusEntityService mybatisPlusEntityService;
    @Resource
    MybatisMapperService mybatisMapperService;
    @Resource
    MybatisXMLService mybatisXMLService;

    @Resource
    MybatisPlusMapperService mybatisPlusMapperService;

    @Resource
    MybatisPlusXmlService mybatisPlusXmlService;

    @Getter
    List<FileCreateService> fileCreateServiceList;

    @PostConstruct
    void init() {
        fileCreateServiceList = List.of(
                ddlService,
                mybatisEntityService,
                mybatisMapperService,
                mybatisXMLService,
                mybatisPlusEntityService,
                mybatisPlusMapperService,
                mybatisPlusXmlService);
    }




}
