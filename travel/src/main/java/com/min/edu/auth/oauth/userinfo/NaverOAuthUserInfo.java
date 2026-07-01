package com.min.edu.auth.oauth.userinfo;

import com.min.edu.auth.entity.AuthProvider;

import java.util.Map;

public class NaverOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    @SuppressWarnings("unchecked")
    public NaverOAuthUserInfo(Map<String, Object> attributes) {
        // Naver는 response 키 안에 실제 사용자 정보가 중첩되어 있음
        this.attributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }
}
