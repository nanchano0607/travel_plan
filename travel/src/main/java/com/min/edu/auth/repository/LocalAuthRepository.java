package com.min.edu.auth.repository;

import com.min.edu.auth.entity.LocalAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalAuthRepository extends JpaRepository<LocalAuth, Long> {
}