package com.school_project.smart_mirror.controller.faceApi;

import java.util.List;
import java.util.Map;

import com.school_project.smart_mirror.dto.face.RecognitionResultDto;
import com.school_project.smart_mirror.service.faceApi.FaceRecognitionEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 얼굴 인식 평가 결과 조회 및 등록을 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
@Slf4j
public class FaceRecognitionEvaluationController {

    private final FaceRecognitionEvaluationService evaluationService;

    /**
     * 인식 결과 기록
     *
     * @param result 인식 결과 데이터
     * @return 처리 결과
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, String>> recordRecognitionResult(
            @RequestBody RecognitionResultDto result) {

        evaluationService.recordRecognitionResult(
                result.getActualUserId(),
                result.getPredictedUserId(),
                result.getSimilarityScore());

        return ResponseEntity.ok(Map.of("status", "recorded"));
    }

    /**
     * 현재 혼동 행렬 조회
     *
     * @return 혼동 행렬 데이터
     */
    @GetMapping("/confusion-matrix")
    public ResponseEntity<Map<String, Integer>> getConfusionMatrix() {
        return ResponseEntity.ok(evaluationService.getConfusionMatrix());
    }

    /**
     * 평가 지표 조회
     *
     * @return 정확도, 정밀도, 재현율, F1 점수 등
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = Map.of(
                "accuracy", evaluationService.calculateAccuracy(),
                "precision", evaluationService.calculatePrecision(),
                "recall", evaluationService.calculateRecall(),
                "f1Score", evaluationService.calculateF1Score(),
                "falsePositiveRate", evaluationService.calculateFalsePositiveRate()
        );

        return ResponseEntity.ok(metrics);
    }

    /**
     * ROC 곡선 데이터 조회
     *
     * @return ROC 곡선 데이터 포인트 목록
     */
    @GetMapping("/roc-curve")
    public ResponseEntity<List<Map<String, Object>>> getROCCurveData() {
        return ResponseEntity.ok(evaluationService.generateROCCurveData());
    }

    /**
     * 최적 임계값 조회
     *
     * @return 사용자별 최적 임계값
     */
    @GetMapping("/optimal-thresholds")
    public ResponseEntity<Map<String, Float>> getOptimalThresholds() {
        return ResponseEntity.ok(evaluationService.calculateOptimalThresholds());
    }

    /**
     * 종합 평가 결과 조회
     *
     * @return 모든 평가 지표와 데이터
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getEvaluationSummary() {
        return ResponseEntity.ok(evaluationService.getEvaluationSummary());
    }

    /**
     * 통계 데이터 초기화
     *
     * @return 처리 결과
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetStatistics() {
        evaluationService.resetStatistics();
        return ResponseEntity.ok(Map.of("status", "reset"));
    }
}
