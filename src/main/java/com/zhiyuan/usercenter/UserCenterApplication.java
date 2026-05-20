package com.zhiyuan.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//springboot的全局启动类（入口）

@SpringBootApplication
@MapperScan("com.zhiyuan.usercenter.mapper")//扫描本地的mapper文件夹，把mapper里的CRUD操作注入到实例中
public class UserCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class, args);
    }

}
