package com.min.edu.auth.oauth;

import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.auth.entity.SocialAuth;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.oauth.userinfo.OAuthUserInfo;
import com.min.edu.auth.repository.SocialAuthRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialLinkService {

    private final UserRepository userRepository;
    private final SocialAuthRepository socialAuthRepository;

    // REQ-AUTH-12: 명시적 소셜 계정 연결
    public void link(Long userId, OAuthUserInfo userInfo) {
        // 이미 다른 계정에 연결된 소셜 계정인지 확인
        socialAuthRepository
                .findByProviderAndProviderUserId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.SOCIAL_ALREADY_LINKED);
                });

        // 같은 provider로 이미 연결된 계정이 있는지 확인
        socialAuthRepository
                .findByUserIdAndProvider(userId, userInfo.getProvider())
                .ifPresent(existing -> {
                    throw new CustomException(ErrorCode.SOCIAL_ALREADY_LINKED);
                });

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        SocialAuth socialAuth = SocialAuth.builder()
                .userId(user.getUserId())
                .provider(userInfo.getProvider())
                .providerUserId(userInfo.getProviderId())
                .providerEmail(userInfo.getEmail())
                .linkedAt(LocalDateTime.now())
                .build();
        socialAuthRepository.save(socialAuth);
    }

    @Transactional(readOnly = true)
    public List<SocialAuth> getLinkedSocials(Long userId) {
        return socialAuthRepository.findByUserId(userId);
    }

    public void unlink(Long userId, AuthProvider provider) {
        SocialAuth socialAuth = socialAuthRepository
                .findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new CustomException(ErrorCode.SOCIAL_NOT_LINKED));

        socialAuthRepository.delete(socialAuth);
    }
}
