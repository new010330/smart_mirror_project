package com.school_project.smart_mirror.dto.admin;

import com.school_project.smart_mirror.domain.admin.Notice;
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

    public static NotificationRespDto noticeToDto(Notice notice) {
        return NotificationRespDto.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .locationName(notice.getLocation_name())
                .startDate(notice.getStart_date())
                .endDate(notice.getEnd_date())
                .build();
    }
}
