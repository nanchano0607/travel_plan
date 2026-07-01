package com.min.edu.auth.oauth;

import com.min.edu.auth.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

// SuccessHandler에서 DB 재조회 없이 userId/role을 바로 꺼내기 위한 래퍼
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Long userId;
    private final String email;
    private final Role role;
    private final boolean linkMode;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;

    public CustomOAuth2User(
            Long userId,
            String email,
            Role role,
            boolean linkMode,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.linkMode = linkMode;
        this.authorities = authorities;
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
