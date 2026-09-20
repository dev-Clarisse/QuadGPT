package com.QuadGPT.backend.auth.repository;

import com.QuadGPT.backend.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
