package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * `@SpringBootApplication` 标注的启动类，**默认只会扫描「启动类自身所在的包」以及它的所有子包**。
 * 可以用指定 scanBasePackages = "entrance" 扫描包路径，{"",""}多个则像这样指定
 */
@SpringBootApplication
public class DesignPatternApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesignPatternApplication.class, args);
    }

}
