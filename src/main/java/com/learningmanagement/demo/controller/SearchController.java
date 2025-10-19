package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.dto.CourseResponse;
import com.learningmanagement.demo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // Advanced search with multiple filters
    @GetMapping("/courses")
    public ResponseEntity<Page<CourseResponse>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CourseResponse> results = searchService.searchCourses(
                keyword, categoryId, difficulty, minPrice, maxPrice, published, pageable
        );

        return ResponseEntity.ok(results);
    }

    // Get recommended courses
    @GetMapping("/recommendations")
    public ResponseEntity<List<CourseResponse>> getRecommendations(
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(searchService.getRecommendedCourses(userId));
    }

    // Get trending courses
    @GetMapping("/trending")
    public ResponseEntity<List<CourseResponse>> getTrendingCourses() {
        return ResponseEntity.ok(searchService.getTrendingCourses());
    }
}
