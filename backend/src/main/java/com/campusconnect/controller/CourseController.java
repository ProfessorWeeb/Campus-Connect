package com.campusconnect.controller;

import com.campusconnect.model.Course;
import com.campusconnect.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Course>> getCoursesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(courseService.getCoursesByDepartment(department));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCourses(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(courseService.searchCourses(query));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Course> getCourseByCode(@PathVariable String code) {
        return ResponseEntity.ok(courseService.getCourseByCode(code));
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initializeCourses() {
        courseService.initializeCourses();
        return ResponseEntity.ok("Courses initialized successfully");
    }
}

