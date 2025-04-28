package com.school_project.smart_mirror.dto.admin.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRespDto {
    private String accessToken;
    private String refreshToken;
}
