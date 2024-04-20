package com.lxkplus.mybatisMaker.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CrontabJobs {

    @Scheduled(cron = "* * * * * *")
    public void Flash() {
        log.info(String.valueOf(System.currentTimeMillis()));
    }

}
