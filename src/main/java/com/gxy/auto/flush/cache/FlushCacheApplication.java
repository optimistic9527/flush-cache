package com.gxy.auto.flush.cache;

import com.gxy.auto.flush.cache.manage.Test2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@MapperScan(basePackages = "com.gxy.auto.flush.cache.mapper")
@EnableKafka
public class FlushCacheApplication {


    public static void main(String[] args) {
        SpringApplication.run(FlushCacheApplication.class, args);
    }

    @Bean
    public Test2 test2() {
        return new Test2("com.gxy.auto.flush.cache");
    }
}

