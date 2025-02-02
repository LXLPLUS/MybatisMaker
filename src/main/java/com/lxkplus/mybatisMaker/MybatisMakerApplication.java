package com.lxkplus.mybatisMaker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.lxkplus.mybatisMaker.Mapper")
public class MybatisMakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MybatisMakerApplication.class, args);
	}
}
