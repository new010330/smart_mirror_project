package com.school_project.smart_mirror.dto.admin.notification;

import jakarta.persistence.Column;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class NotificationRequestDto {
    private String title;
    private String content;
    private String locationName;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
