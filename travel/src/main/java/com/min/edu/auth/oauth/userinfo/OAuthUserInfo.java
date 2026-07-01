package com.min.edu.auth.oauth.userinfo;

import com.min.edu.auth.entity.AuthProvider;

public interface OAuthUserInfo {

    String getProviderId();
    String getEmail();
    String getName();
    AuthProvider getProvider();
}
