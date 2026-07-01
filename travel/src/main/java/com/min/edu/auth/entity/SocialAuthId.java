package com.min.edu.auth.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class SocialAuthId implements Serializable {

    private Long userId;
    private AuthProvider provider;
}
