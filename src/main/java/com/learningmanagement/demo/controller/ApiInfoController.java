package com.learningmanagement.demo.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiInfoController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "EduSmart API");
        info.put("version", "1.0.0");
        info.put("description", "Smart E-Learning Platform REST API");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Authentication", "/api/auth/**");
        endpoints.put("Courses", "/api/courses/**");
        endpoints.put("Enrollments", "/api/enrollments/**");
        endpoints.put("Forum", "/api/forum/**");
        endpoints.put("Quizzes", "/api/quizzes/**");
        endpoints.put("Search", "/api/search/**");
        endpoints.put("Notifications", "/api/notifications/**");
        endpoints.put("Statistics", "/api/statistics/**");

        info.put("endpoints", endpoints);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "EduSmart API is running");
        return ResponseEntity.ok(health);
    }
}
