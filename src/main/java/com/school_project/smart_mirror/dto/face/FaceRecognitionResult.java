package com.school_project.smart_mirror.dto.face;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 얼굴 인식 결과 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognitionResult {

    /**
     * 인식된 사용자 ID (없으면 null)
     */
    private String userId;

    /**
     * 유사도 점수 (0.0 ~ 1.0)
     */
    private float similarityScore;

    /**
     * 인식 처리 시간 (밀리초)
     */
    private long processingTimeMs;

    /**
     * 사용자 인식 여부
     */
    private boolean recognized;

    /**
     * 추가 메타데이터 (선택적)
     */
    private Map<String, Object> metadata;
}
