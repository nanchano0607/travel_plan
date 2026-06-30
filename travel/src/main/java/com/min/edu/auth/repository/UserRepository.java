package com.min.edu.auth.repository;

import com.min.edu.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<UserEntity> findByEmail(String email);
}
