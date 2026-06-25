package com.min.edu.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "local_auth")
@Getter
@Setter
@NoArgsConstructor
public class LocalAuth {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password", nullable = false)
    private String password;
}
