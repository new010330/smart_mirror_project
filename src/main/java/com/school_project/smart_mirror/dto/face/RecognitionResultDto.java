package com.school_project.smart_mirror.dto.face;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 얼굴 인식 결과 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognitionResultDto {

    /**
     * 실제 사용자 ID (테스트/평가 시 입력)
     * null인 경우 미등록 사용자로 간주
     */
    private String actualUserId;

    /**
     * 시스템이 예측한 사용자 ID
     * null인 경우 사용자를 식별하지 못했음을 의미
     */
    private String predictedUserId;

    /**
     * 유사도 점수 (0.0 ~ 1.0)
     */
    private float similarityScore;

    /**
     * 인식 수행 시간 (밀리초)
     */
    private long processingTimeMs;

    /**
     * 사용된 임계값
     */
    private float threshold;

    /**
     * 평가 태그 (예: "정면", "측면", "어두운 조명" 등)
     */
    private String evaluationTag;
}
