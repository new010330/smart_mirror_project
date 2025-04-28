package com.school_project.smart_mirror.dto.admin.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserInfo {
    private String locationName;
    private Double latitude;
    private Double longitude;
    private Object features;
    private String username;
}