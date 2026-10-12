package com.QuadGPT.backend.User.Admin;


import com.QuadGPT.backend.department.Department;
import com.QuadGPT.backend.User.entity.Role;
import com.QuadGPT.backend.User.entity.User;
import com.QuadGPT.backend.User.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminDepartment;

    public AdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@quadgpt.com}") String adminEmail,
            @Value("${app.admin.password:ChangeMe123!}") String adminPassword,
            @Value("${app.admin.department:IT}") String adminDepartment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminDepartment = adminDepartment;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin {} already exists — skipping.", adminEmail);
            return;
        }
        User admin = new User(
                adminEmail,
                passwordEncoder.encode(adminPassword),
                Department.valueOf(adminDepartment),
                Role.ADMIN
        );
        userRepository.save(admin);
        log.warn("Default admin created: {} — CHANGE THIS PASSWORD IN PRODUCTION.", adminEmail);
    }
}
