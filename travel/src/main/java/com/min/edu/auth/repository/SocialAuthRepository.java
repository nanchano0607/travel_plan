package com.min.edu.auth.repository;

import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.auth.entity.SocialAuth;
import com.min.edu.auth.entity.SocialAuthId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, SocialAuthId> {

    Optional<SocialAuth> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<SocialAuth> findByUserIdAndProvider(Long userId, AuthProvider provider);

    List<SocialAuth> findByUserId(Long userId);
}
