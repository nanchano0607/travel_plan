package com.min.edu.auth.oauth.userinfo;

import com.min.edu.auth.entity.AuthProvider;

import java.util.Map;

public class GoogleOAuthUserInfo implements OAuthUserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuthUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
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
        return AuthProvider.GOOGLE;
    }
}
