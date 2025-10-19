package com.learningmanagement.demo.config;


import com.learningmanagement.demo.service.CategoryService;
import com.learningmanagement.demo.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmupRunner implements CommandLineRunner {

    private final CourseService courseService;
    private final CategoryService categoryService;

    @Override
    public void run(String... args) {
        log.info("Starting cache warmup...");

        try {
            // Warmup: Load all published courses
            log.info("Warming up published courses cache");
            courseService.getAllPublishedCourses();

            // Warmup: Load all categories
            log.info("Warming up categories cache");
            categoryService.getAllCategories();

            // Warmup: Load courses for each category
            categoryService.getAllCategories().forEach(category -> {
                log.info("Warming up courses for category: {}", category.getName());
                courseService.getCoursesByCategory(category.getId());
            });

            log.info("Cache warmup completed successfully!");

        } catch (Exception e) {
            log.error("Error during cache warmup: {}", e.getMessage());
        }
    }
}
