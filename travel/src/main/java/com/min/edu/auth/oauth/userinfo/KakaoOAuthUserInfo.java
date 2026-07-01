package com.min.edu.auth.oauth.userinfo;

import com.min.edu.auth.entity.AuthProvider;

import java.util.Map;

public class KakaoOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    // kakao_account.email
    @SuppressWarnings("unchecked")
    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }

        return (String) kakaoAccount.get("email");
    }

    // kakao_account.profile.nickname
    @SuppressWarnings("unchecked")
    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            return null;
        }

        return (String) profile.get("nickname");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }
}
