package com.campusconnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusconnect.model.Course;
import com.campusconnect.repository.CourseRepository;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findByActiveTrue();
    }

    public List<Course> getCoursesByDepartment(String department) {
        return courseRepository.findByDepartment(department);
    }

    public List<Course> searchCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    public Course getCourseByCode(String code) {
        return courseRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Course not found: " + code));
    }

    @Transactional
    public void initializeCourses() {
        // Check if courses already exist
        if (courseRepository.count() > 0) {
            return; // Courses already initialized
        }

        // CSCI Courses
        createCourse("CSCI 1301", "Computer Science I", "CSCI");
        createCourse("CSCI 1302", "Computer Science II", "CSCI");
        createCourse("CSCI 2201", "Principles of Programming Languages", "CSCI");
        createCourse("CSCI 2205", "Introduction to Data Structures and Algorithms", "CSCI");
        createCourse("CSCI 2207", "Ethics in Computer Science", "CSCI");
        createCourse("CSCI 2300", "Experiential Learning in CS", "CSCI");
        createCourse("CSCI 3235", "Human Computer Interaction", "CSCI");
        createCourse("CSCI 3245", "Database Principles", "CSCI");
        createCourse("CSCI 3250", "Software Engineering", "CSCI");
        createCourse("CSCI 3251", "Object-Oriented Programming", "CSCI");
        createCourse("CSCI 3265", "Operating Systems", "CSCI");
        createCourse("CSCI 3400", "Introduction to AI", "CSCI");
        createCourse("CSCI 3410", "AI and Cybersecurity", "CSCI");
        createCourse("CSCI 4230", "Graphic Imaging", "CSCI");
        createCourse("CSCI 4237", "3D Modeling and Animation", "CSCI");
        createCourse("CSCI 4238", "2D Computer Animation", "CSCI");
        createCourse("CSCI 4250", "Computational Intelligence", "CSCI");
        createCourse("CSCI 4255", "Game Design and Development", "CSCI");
        createCourse("CSCI 4264", "Data Structures and Algorithm Analysis", "CSCI");
        createCourse("CSCI 4270", "Robot Programming", "CSCI");
        createCourse("CSCI 4299", "Special Topics of Computer Science", "CSCI");
        createCourse("CSCI 4361", "Software Security", "CSCI");
        createCourse("CSCI 4362", "Computer Architecture", "CSCI");
        createCourse("CSCI 4365", "Software Testing and Quality Assurance", "CSCI");
        createCourse("CSCI 4451", "Introduction to HCI Design", "CSCI");
        createCourse("CSCI 4452", "HCI Methods – Design and Evaluation", "CSCI");
        createCourse("CSCI 4453", "Web Application Design", "CSCI");
        createCourse("CSCI 4454", "Human Robot Interaction", "CSCI");
        createCourse("CSCI 4461", "AI: Implications and Applications", "CSCI");
        createCourse("CSCI 4462", "Digital Transformation and AI", "CSCI");
        createCourse("CSCI 4463", "Machine Learning", "CSCI");
        createCourse("CSCI 4464", "Deep Learning", "CSCI");
        createCourse("CSCI 4465", "Natural Language Processing", "CSCI");
        createCourse("CSCI 4701", "Internship in Computer Science", "CSCI");
        createCourse("CSCI 4750", "Senior Capstone", "CSCI");
        
        // ITEC Courses
        createCourse("ITEC 1001", "Perspectives on Computing in Society", "ITEC");
        createCourse("ITEC 2201", "Business Information Applications", "ITEC");
        createCourse("ITEC 2215", "Introduction to Information Technology", "ITEC");
        createCourse("ITEC 2245", "Introduction to Databases for Health Sciences", "ITEC");
        createCourse("ITEC 2260", "Introduction to Computer Programming", "ITEC");
        createCourse("ITEC 2270", "Application Development", "ITEC");
        createCourse("ITEC 2299", "Special Topics in Information Technology", "ITEC");
        createCourse("ITEC 2320", "Network Essentials", "ITEC");
        createCourse("ITEC 2340", "Introduction to Cybersecurity", "ITEC");
        createCourse("ITEC 2380", "Web Development", "ITEC");
        createCourse("ITEC 2510", "Agile Software Development", "ITEC");
        createCourse("ITEC 2520", "Object-Oriented Software Development", "ITEC");
        createCourse("ITEC 2530", "Software Testing and Test-Driven Development", "ITEC");
        createCourse("ITEC 3100", "Python Scripting", "ITEC");
        createCourse("ITEC 3155", "Systems Analysis and Design", "ITEC");
        createCourse("ITEC 3220", "Hardware and Systems Software", "ITEC");
        createCourse("ITEC 3235", "Human Computer Interaction", "ITEC");
        createCourse("ITEC 3236", "Interactive Digital Media", "ITEC");
        createCourse("ITEC 3245", "Database Principles", "ITEC");
        createCourse("ITEC 3250", "Software Engineering", "ITEC");
        createCourse("ITEC 3265", "Operating Systems", "ITEC");
        createCourse("ITEC 3280", "Web Programming", "ITEC");
        createCourse("ITEC 3300", "Project Management", "ITEC");
        createCourse("ITEC 3310", "Information Technology and Organizational Integration", "ITEC");
        createCourse("ITEC 3325", "Windows Systems Administration", "ITEC");
        createCourse("ITEC 3328", "Linux Systems Administration", "ITEC");
        createCourse("ITEC 3340", "Business Analysis Using Excel", "ITEC");
        createCourse("ITEC 3351", "Analytics and Organizational Intelligence", "ITEC");
        createCourse("ITEC 3355", "Data Mining", "ITEC");
        createCourse("ITEC 3400", "Technology Entrepreneurship", "ITEC");
        createCourse("ITEC 3405", "Creativity & Innovation", "ITEC");
        createCourse("ITEC 3410", "Startups Financing and Marketing Strategies", "ITEC");
        createCourse("ITEC 3415", "Managing & Growing an IT Venture", "ITEC");
        createCourse("ITEC 3502", "Data Architecture", "ITEC");
        createCourse("ITEC 3505", "Data Management", "ITEC");
        createCourse("ITEC 3508", "Data-driven Decision Making", "ITEC");
        createCourse("ITEC 4061", "Coding Fundamentals", "ITEC");
        createCourse("ITEC 4063", "Scripting", "ITEC");
        createCourse("ITEC 4065", "Mobile App Coding", "ITEC");
        createCourse("ITEC 4067", "Secure Coding", "ITEC");
        createCourse("ITEC 4200", "Foundations of Information Security", "ITEC");
        createCourse("ITEC 4205", "Legal and Ethical Issues", "ITEC");
        createCourse("ITEC 4210", "Intrusion Detection/Prevention Systems", "ITEC");
        createCourse("ITEC 4230", "Graphic Imaging", "ITEC");
        createCourse("ITEC 4231", "Design Content for Instructional Applications", "ITEC");
        createCourse("ITEC 4237", "3D Modeling and Animation", "ITEC");
        createCourse("ITEC 4238", "2D Computer Animation", "ITEC");
        createCourse("ITEC 4242", "Database Administration", "ITEC");
        createCourse("ITEC 4244", "Database Programming", "ITEC");
        createCourse("ITEC 4250", "Computational Intelligence", "ITEC");
        createCourse("ITEC 4254", "Business Driven Technology", "ITEC");
        createCourse("ITEC 4255", "Game Design and Development", "ITEC");
        createCourse("ITEC 4261", "Intro to JAVA Programming", "ITEC");
        createCourse("ITEC 4264", "Data Structures and Algorithm Analysis", "ITEC");
        createCourse("ITEC 4266", "C++ Programming", "ITEC");
        createCourse("ITEC 4269", "Client/Server Systems Programming", "ITEC");
        createCourse("ITEC 4270", "Robot Programming", "ITEC");
        createCourse("ITEC 4284", "Web Multimedia Delivery", "ITEC");
        createCourse("ITEC 4285", "Network Services", "ITEC");
        createCourse("ITEC 4286", "Web Applications Development", "ITEC");
        createCourse("ITEC 4288", "Electronic Commerce Systems", "ITEC");
        createCourse("ITEC 4299", "Special Topics in Information Technology", "ITEC");
        createCourse("ITEC 4310", "Critical Infrastructure Security and Resilience", "ITEC");
        createCourse("ITEC 4320", "Industrial Control Systems Security", "ITEC");
        createCourse("ITEC 4321", "Forensics/Data Recovery", "ITEC");
        createCourse("ITEC 4322", "Advanced Digital Forensics", "ITEC");
        createCourse("ITEC 4323", "Mobile Forensics", "ITEC");
        createCourse("ITEC 4329", "Data Communications", "ITEC");
        createCourse("ITEC 4330", "Routing and Switching", "ITEC");
        createCourse("ITEC 4332", "Firewalls and VPNs", "ITEC");
        createCourse("ITEC 4341", "Network Forensics and Incident Response Planning", "ITEC");
        createCourse("ITEC 4344", "Ethical Hacking", "ITEC");
        createCourse("ITEC 4361", "Software Security", "ITEC");
        createCourse("ITEC 4362", "Computer Architecture", "ITEC");
        createCourse("ITEC 4364", "Systems Programming", "ITEC");
        createCourse("ITEC 4365", "Test-Driven Software Development", "ITEC");
        createCourse("ITEC 4366", "Advanced Web Development", "ITEC");
        createCourse("ITEC 4367", "Advanced Software Engineering", "ITEC");
        createCourse("ITEC 4370", "Virtual Computing", "ITEC");
        createCourse("ITEC 4375", "Cloud Computing", "ITEC");
        createCourse("ITEC 4421", "Network Security", "ITEC");
        createCourse("ITEC 4501", "Special Projects in Information Technology", "ITEC");
        createCourse("ITEC 4701", "Internship in Information Technology", "ITEC");
        createCourse("ITEC 4710", "Globalization and Technology", "ITEC");
        createCourse("ITEC 4750", "Senior Capstone", "ITEC");
        createCourse("ITEC 4760", "Business Plan Development", "ITEC");
        createCourse("ITEC 4770", "Experiential Learning Practicum", "ITEC");
        
        // ENGR Courses
        createCourse("ENGR 1001K", "Introduction to Engineering", "ENGR");
        createCourse("ENGR 1001L", "Introduction to Engineering Lab", "ENGR");
        createCourse("ENGR 1002", "Engineering Design Graphics", "ENGR");
        createCourse("ENGR 1091", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 1092", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 1093", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 1100K", "Introduction to Computer Engineering", "ENGR");
        createCourse("ENGR 1100L", "Introduction to Computer Engineer Lab", "ENGR");
        createCourse("ENGR 2025K", "Introduction to Signal Processing", "ENGR");
        createCourse("ENGR 2025L", "Introduction to Signal Process Lab", "ENGR");
        createCourse("ENGR 2040", "Circuit Analysis", "ENGR");
        createCourse("ENGR 2091", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 2092", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 2093", "Cooperative Education Work Experience", "ENGR");
        createCourse("ENGR 2210", "Statics", "ENGR");
        createCourse("ENGR 2220", "Dynamics", "ENGR");
        createCourse("ENGR 2230", "Mechanics of Deformable Bodies", "ENGR");
        createCourse("ENGR 2300", "Principles of Engineering Economy", "ENGR");
        createCourse("ENGR 2500K", "Surveying and Geomatics", "ENGR");
        createCourse("ENGR 2500L", "Surveying and Geomatics", "ENGR");
        createCourse("ENGR 2600", "Thermodynamics", "ENGR");
        
        // PHYS Courses
        createCourse("PHYS 1011K", "Physical Science I", "PHYS");
        createCourse("PHYS 1011L", "Physical Science I Lab", "PHYS");
        createCourse("PHYS 1012K", "Physical Science II", "PHYS");
        createCourse("PHYS 1012L", "Physical Science II Lab", "PHYS");
        createCourse("PHYS 1111K", "Introductory Physics I", "PHYS");
        createCourse("PHYS 1111L", "Introductory Physics I LAB", "PHYS");
        createCourse("PHYS 1112K", "Introductory Physics II", "PHYS");
        createCourse("PHYS 1112L", "Introductory Physics II LAB", "PHYS");
        createCourse("PHYS 2211K", "Principles of Physics I", "PHYS");
        createCourse("PHYS 2211L", "Principles of Physics I LAB", "PHYS");
        createCourse("PHYS 2212K", "Principles of Physics II", "PHYS");
        createCourse("PHYS 2212L", "Principles of Physics II LAB", "PHYS");
        createCourse("PHYS 2999", "Special Topics in Physics", "PHYS");
        
        // MATH Courses
        createCourse("MATH 0996", "Support for Elementary Statistics", "MATH");
        createCourse("MATH 0997", "Support for Quantitative Reasoning", "MATH");
        createCourse("MATH 0998", "Support for Mathematical Modeling", "MATH");
        createCourse("MATH 0999", "Support for College Algebra", "MATH");
        createCourse("MATH 1001", "Quantitative Reasoning", "MATH");
        createCourse("MATH 1003", "Perspectives on Mathematics", "MATH");
        createCourse("MATH 1101", "Introduction to Mathematical Modeling", "MATH");
        createCourse("MATH 1111", "College Algebra", "MATH");
        createCourse("MATH 1112", "Plane Trigonometry", "MATH");
        createCourse("MATH 1113", "Precalculus Mathematics", "MATH");
        createCourse("MATH 1113H", "Honors Precalculus", "MATH");
        createCourse("MATH 1251", "Calculus I", "MATH");
        createCourse("MATH 1371", "Computing for the Mathematical Sciences", "MATH");
        createCourse("MATH 1401", "Elementary Statistics", "MATH");
        createCourse("MATH 1401H", "Honors Elementary Statistics", "MATH");
        createCourse("MATH 1501", "Calculus I", "MATH");
        createCourse("MATH 2008", "Foundations of Numbers and Operations", "MATH");
        createCourse("MATH 2120", "Discrete Mathematics", "MATH");
        createCourse("MATH 2252", "Calculus II", "MATH");
        createCourse("MATH 2253", "Calculus III", "MATH");
        createCourse("MATH 2260", "Introduction to Linear Algebra", "MATH");
        createCourse("MATH 2270", "Differential Equations", "MATH");
        createCourse("MATH 2401", "Elementary Statistics II", "MATH");
        createCourse("MATH 3010", "History of Mathematics", "MATH");
        createCourse("MATH 3040", "Bridge to Higher Mathematics", "MATH");
        createCourse("MATH 3207", "Communicating Mathematics", "MATH");
        createCourse("MATH 3251", "Applied Combinatorics", "MATH");
        createCourse("MATH 3260", "Modern Algebra", "MATH");
        createCourse("MATH 3262", "Modern Algebra II", "MATH");
        createCourse("MATH 3270", "Differential Equations II with Modeling", "MATH");
        createCourse("MATH 3280", "Complex Analysis", "MATH");
        createCourse("MATH 3440", "Data Exploration and Visualization", "MATH");
        createCourse("MATH 3450", "Applied Statistics for Big Data Analysis", "MATH");
        createCourse("MATH 3500", "Applied Probability", "MATH");
        createCourse("MATH 3510", "Foundations of Geometry", "MATH");
        createCourse("MATH 3600", "Probability and Statistics", "MATH");
        createCourse("MATH 3610", "Biological Statistics", "MATH");
        createCourse("MATH 3700", "Applied Calculus for Information Technology", "MATH");
        createCourse("MATH 3999", "Special Topics in Mathematics", "MATH");
        createCourse("MATH 4110", "Number Theory", "MATH");
        createCourse("MATH 4150", "Linear Algebra", "MATH");
        createCourse("MATH 4260", "Mathematical Analysis", "MATH");
        createCourse("MATH 4270", "Point Set Topology", "MATH");
        createCourse("MATH 4300", "Regression Analysis", "MATH");
        createCourse("MATH 4480", "Graph Theory", "MATH");
        createCourse("MATH 4600", "Probability and Statistics II", "MATH");
        createCourse("MATH 4611", "Applied Statistics I", "MATH");
        createCourse("MATH 4612", "Applied Statistics II", "MATH");
        createCourse("MATH 4621", "Mathematical Statistics I", "MATH");
        createCourse("MATH 4622", "Mathematical Statistics II", "MATH");
        createCourse("MATH 4630", "Topics in Applied Statistics", "MATH");
        createCourse("MATH 4651", "Numerical Analysis I", "MATH");
        createCourse("MATH 4700", "Introduction to Experimental Design", "MATH");
        createCourse("MATH 4760", "Data Applications and Ethics", "MATH");
        createCourse("MATH 4900", "Internship in Mathematics", "MATH");
        createCourse("MATH 4901", "Operations Research I", "MATH");
        createCourse("MATH 4902", "Operations Research II", "MATH");
        createCourse("MATH 4905", "Optimization", "MATH");
    }

    private void createCourse(String code, String name, String department) {
        Course course = new Course();
        course.setCode(code);
        course.setName(name);
        course.setDepartment(department);
        course.setActive(true);
        courseRepository.save(course);
    }
}

