package com.min.edu.auth.repository;

import com.min.edu.auth.entity.SocialAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {
    Optional<SocialAuth> findByProviderAndProviderUserId(String provider, String providerUserId);
}
