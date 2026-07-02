package com.min.edu.auth.oauth;

import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.auth.entity.SocialAuth;
import com.min.edu.auth.entity.UserEntity;
import com.min.edu.auth.oauth.userinfo.OAuthUserInfo;
import com.min.edu.auth.oauth.userinfo.OAuthUserInfoFactory;
import com.min.edu.auth.repository.SocialAuthRepository;
import com.min.edu.auth.repository.UserRepository;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final SocialAuthRepository socialAuthRepository;
    private final SocialLinkService socialLinkService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

        String nameAttributeKey = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthUserInfo userInfo = OAuthUserInfoFactory.of(provider, oAuth2User.getAttributes());

        // 세션에서 연결 모드 확인 (SocialLinkController.startLink에서 저장)
        HttpSession session = getCurrentSession();
        Long linkUserId = session != null ? (Long) session.getAttribute("SOCIAL_LINK_USER_ID") : null;

        UserEntity user;
        boolean isLinkMode;

        if (linkUserId != null) {
            // 연결 모드: CustomException → OAuth2AuthenticationException으로 변환해 FailureHandler로 전달
            try {
                socialLinkService.link(linkUserId, userInfo);
            } catch (CustomException e) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(e.getErrorCode().name().toLowerCase()),
                        e.getMessage()
                );
            }

            user = userRepository.findById(linkUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            isLinkMode = true;
        } else {
            // 로그인 모드
            user = saveOrUpdate(userInfo);
            isLinkMode = false;
        }

        return new CustomOAuth2User(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                isLinkMode,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                nameAttributeKey
        );
    }

    // provider + provider_user_id로 기존 연결 조회 → 없으면 신규 계정 생성
    private UserEntity saveOrUpdate(OAuthUserInfo userInfo) {

        return socialAuthRepository
                .findByProviderAndProviderUserId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .map(socialAuth ->
                        userRepository.findById(socialAuth.getUserId())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND))
                )
                .orElseGet(() -> {
                    // 이메일 일치만으로 자동 연결하지 않음
                    if (userRepository.existsByEmail(userInfo.getEmail())) {
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error("email_conflict"),
                                ErrorCode.SOCIAL_EMAIL_CONFLICT.getMessage()
                        );
                    }

                    return createUser(userInfo);
                });
    }

    // @Transactional 보장으로 실패 시 user_auth + social_auth 동시 롤백
    private UserEntity createUser(OAuthUserInfo userInfo) {
        String nickname;
        do {
            nickname = NicknameGenerator.generate();
        } while (userRepository.existsByNickname(nickname));

        UserEntity user = UserEntity.builder()
                .email(userInfo.getEmail())
                .provider(userInfo.getProvider())
                .name(userInfo.getName())
                .nickname(nickname)
                .build();
        userRepository.save(user);

        SocialAuth socialAuth = SocialAuth.builder()
                .userId(user.getUserId())
                .provider(userInfo.getProvider())
                .providerUserId(userInfo.getProviderId())
                .providerEmail(userInfo.getEmail())
                .linkedAt(LocalDateTime.now())
                .build();
        socialAuthRepository.save(socialAuth);

        return user;
    }

    private HttpSession getCurrentSession() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }

        HttpServletRequest request = attrs.getRequest();

        return request.getSession(false);
    }
}
