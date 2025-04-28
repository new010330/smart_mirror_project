package com.school_project.smart_mirror.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FaceSearchRequestDto {
    // 얼굴 인식용 벡터 필드
    private List<Float> vector;


}
