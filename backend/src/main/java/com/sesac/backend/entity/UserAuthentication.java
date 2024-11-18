package com.sesac.backend.entity;

import com.sesac.backend.board.constant.SocialProvider;
import com.sesac.backend.board.constant.UserType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthentication extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID userId; // 사용자ID

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    private String passwordHash; // 비밀번호해시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType; // 사용자유형

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider; // 소셜로그인제공자

    private String socialId; // 소셜ID

    @Column(nullable = false)
    private boolean isActive = true; // 활성화여부

    private LocalDateTime lastLogin; // 마지막로그인
}
