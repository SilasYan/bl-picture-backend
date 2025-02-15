package com.baolong.blpicturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.baolong.blpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 通过 AOP 提供对当前代理对象的访问, 通过 AopContext.currentProxy() 获取当前代理对象
public class BlPictureBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlPictureBackendApplication.class, args);
	}

}
