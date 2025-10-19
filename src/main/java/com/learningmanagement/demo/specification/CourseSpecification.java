package com.learningmanagement.demo.specification;



import com.learningmanagement.demo.entity.Course;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CourseSpecification {

    public static Specification<Course> filterCourses(
            String keyword,
            Long categoryId,
            String difficulty,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean published) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Keyword search in title or description
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        likePattern
                );
                Predicate descMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        likePattern
                );
                predicates.add(criteriaBuilder.or(titleMatch, descMatch));
            }

            // Filter by category
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter by difficulty
            if (difficulty != null && !difficulty.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
            }

            // Filter by price range
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Filter by published status
            if (published != null) {
                predicates.add(criteriaBuilder.equal(root.get("published"), published));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
