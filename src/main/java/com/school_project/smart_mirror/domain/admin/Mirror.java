package com.school_project.smart_mirror.domain.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "mirrors")
@Getter
@Setter
@NoArgsConstructor
public class Mirror {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Integer mirror_id;

    @Column
    private String location_name;
    @Column
    private Double latitude;
    @Column
    private Double longitude;
    @Column
    private String features;

    @Column
    @CreationTimestamp
    private LocalDate created_at;

    @Column
    @UpdateTimestamp
    private LocalDate updated_at;


}
