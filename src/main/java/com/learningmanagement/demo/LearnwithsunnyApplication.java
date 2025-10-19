package com.learningmanagement.demo;

import com.learningmanagement.demo.controller.initiation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableCaching
public class LearnwithsunnyApplication {

	public static void main(String[] args) {

        ApplicationContext context= SpringApplication.run(LearnwithsunnyApplication.class, args);
        initiation obj=context.getBean("initiation",initiation.class);
        obj.print();
	}

}
