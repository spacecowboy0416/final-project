package com.finalproject.coordi;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class CoordiApplication {

    @PostConstruct
    public void started() {
        // 애플리케이션 전체의 타임존을 한국 시간으로 고정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
    public static void main(String[] args) {
        SpringApplication.run(CoordiApplication.class, args);
    }
}
>>>>>>> develop
