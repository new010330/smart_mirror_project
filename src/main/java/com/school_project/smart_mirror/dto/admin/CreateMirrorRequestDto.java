package com.school_project.smart_mirror.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMirrorRequestDto {
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String features;

    private String username;
    private String password;
}
