package com.school_project.smart_mirror.dto.admin;

import com.school_project.smart_mirror.domain.admin.Mirror;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UserInfoRespDto {
    private String locationName;
    private Double latitude;
    private Double longitude;
    private Object features;
    private String username;
}