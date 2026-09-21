package com.mofei.erp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mofei.erp.modules.**.mapper")
public class MofeiErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MofeiErpApplication.class, args);
    }
}
