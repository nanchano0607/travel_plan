package com.min.edu.auth.oauth.userinfo;

import com.min.edu.auth.entity.AuthProvider;
import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;

import java.util.Map;

public class OAuthUserInfoFactory {

    public static OAuthUserInfo of(
            AuthProvider provider,
            Map<String, Object> attributes
    ) {

        return switch (provider) {
            case GOOGLE -> new GoogleOAuthUserInfo(attributes);
            case NAVER -> new NaverOAuthUserInfo(attributes);
            case KAKAO -> new KakaoOAuthUserInfo(attributes);
            default -> throw new CustomException(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
        };
    }
}
