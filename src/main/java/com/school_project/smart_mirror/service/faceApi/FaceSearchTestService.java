package com.school_project.smart_mirror.service.faceApi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.school_project.smart_mirror.dto.face.FaceRecognitionResult;
import com.school_project.smart_mirror.dto.face.RecognitionResultDto;
import com.school_project.smart_mirror.repository.faceApi.FaceRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 얼굴 인식 서비스 내에서 혼동 행렬을 활용한 예시
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FaceSearchTestService {

    private final FaceRepository faceRepository;
    private final FaceRecognitionEvaluationService evaluationService;

    private static final float DEFAULT_SIMILARITY_THRESHOLD = 0.75f;

    /**
     * 얼굴 인식 및 평가
     *
     * @param faceEmbedding 얼굴 임베딩 벡터
     * @param actualUserId 실제 사용자 ID (테스트/평가 시에만 사용)
     * @return 인식 결과
     */
    public FaceRecognitionResult recognizeFace(float[] faceEmbedding, String actualUserId) {
        long startTime = System.currentTimeMillis();

        // 유사 얼굴 검색
        Map<String, Float> similarFaces = searchSimilarFaces(faceEmbedding);

        // 가장 유사한 얼굴 찾기
        Optional<Map.Entry<String, Float>> bestMatch = similarFaces.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        // 결과 생성
        String predictedUserId = null;
        float similarityScore = 0f;

        if (bestMatch.isPresent() && bestMatch.get().getValue() >= DEFAULT_SIMILARITY_THRESHOLD) {
            predictedUserId = bestMatch.get().getKey();
            similarityScore = bestMatch.get().getValue();
        }

        long processingTime = System.currentTimeMillis() - startTime;

        // 평가 데이터 기록 (테스트 모드일 때만)
        if (actualUserId != null || isTestMode()) {
            recordEvaluationData(actualUserId, predictedUserId, similarityScore, processingTime);
        }

        // 결과 반환
        return FaceRecognitionResult.builder()
                .userId(predictedUserId)
                .similarityScore(similarityScore)
                .processingTimeMs(processingTime)
                .recognized(predictedUserId != null)
                .build();
    }

    /**
     * 유사 얼굴 검색 (Elasticsearch 활용)
     */
    private Map<String, Float> searchSimilarFaces(float[] faceEmbedding) {
        // 실제 구현은 Elasticsearch를 통한 검색 로직 포함
        // 여기서는 간소화를 위해 더미 결과 반환
        return Collections.emptyMap();
    }

    /**
     * 평가 데이터 기록
     */
    private void recordEvaluationData(String actualUserId, String predictedUserId,
                                      float similarityScore, long processingTime) {

        RecognitionResultDto evaluationResult = RecognitionResultDto.builder()
                .actualUserId(actualUserId)
                .predictedUserId(predictedUserId)
                .similarityScore(similarityScore)
                .processingTimeMs(processingTime)
                .threshold(DEFAULT_SIMILARITY_THRESHOLD)
                .evaluationTag("standard")
                .build();

        // 평가 서비스에 결과 기록
        evaluationService.recordRecognitionResult(
                evaluationResult.getActualUserId(),
                evaluationResult.getPredictedUserId(),
                evaluationResult.getSimilarityScore());

        // 로그 기록
        log.debug("Face recognition evaluation recorded: actual={}, predicted={}, similarity={}",
                actualUserId, predictedUserId, similarityScore);
    }

    /**
     * 테스트 모드 여부 확인
     */
    private boolean isTestMode() {
        // 테스트 모드 확인 로직 구현
        // 예: 프로파일 확인, 환경 변수 확인 등
        return false;
    }

    /**
     * 성능 최적화를 위한 임계값 조정
     */
    public void optimizeThresholds() {
        // 혼동 행렬 분석 결과를 이용한 임계값 최적화
        Map<String, Float> optimalThresholds = evaluationService.calculateOptimalThresholds();

        // 전역 임계값 또는 사용자별 임계값 업데이트 로직
        log.info("Optimized thresholds: {}", optimalThresholds);

        // 임계값 업데이트 로직 구현
        // ...
    }

    /**
     * 현재 인식 시스템 성능 보고서 생성
     */
    public Map<String, Object> generatePerformanceReport() {
        return evaluationService.getEvaluationSummary();
    }
}