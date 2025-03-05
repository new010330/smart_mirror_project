package com.school_project.smart_mirror.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class NotificationRespDto {
    private String title;
    private String content;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;
}
