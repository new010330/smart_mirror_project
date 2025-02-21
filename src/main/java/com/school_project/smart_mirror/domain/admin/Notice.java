package com.school_project.smart_mirror.domain.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer announcement_id;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private String location_name;

    @Column
    private LocalDate start_date;

    @Column
    private LocalDate end_date;

    @Column
    @CreationTimestamp
    private LocalDate created_at;

    @Column
    @UpdateTimestamp
    private LocalDate updated_at;
}
