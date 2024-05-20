package com.lxkplus.mybatisMaker;

import com.lxkplus.mybatisMaker.job.CrontabJobs;
import com.lxkplus.mybatisMaker.service.PersistenceService;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class Test {

    @Resource
    CrontabJobs crontabJobs;

    @Resource
    PersistenceService persistenceService;
    

    @org.junit.jupiter.api.Test
    public void test() throws IOException {
        persistenceService.clearCache();
        crontabJobs.flash();

    }
}
