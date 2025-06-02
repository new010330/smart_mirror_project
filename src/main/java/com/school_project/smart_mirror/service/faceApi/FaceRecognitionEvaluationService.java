package com.school_project.smart_mirror.service.faceApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 얼굴 인식 시스템의 성능을 평가하기 위한 혼동 행렬 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FaceRecognitionEvaluationService {

    // TP: 올바르게 등록된 사용자를 식별한 경우
    // TN: 올바르게 미등록 사용자를 식별한 경우
    // FP: 미등록 사용자를 등록된 사용자로 잘못 식별한 경우
    // FN: 등록된 사용자를 식별하지 못한 경우
    private final Map<String, Integer> confusionMatrix = new ConcurrentHashMap<>();

    // 사용자 ID별 인식 결과 통계
    private final Map<String, UserRecognitionStats> userStats = new ConcurrentHashMap<>();

    // 사용자 ID별 임계값 최적화 데이터
    private final Map<String, Map<Float, ThresholdStats>> thresholdOptimizationData = new ConcurrentHashMap<>();

    /**
     * 혼동 행렬 초기화
     */
    public void initializeConfusionMatrix() {
        confusionMatrix.put("TP", 0);
        confusionMatrix.put("TN", 0);
        confusionMatrix.put("FP", 0);
        confusionMatrix.put("FN", 0);
    }

    /**
     * 인식 결과 기록
     *
     * @param actualUserId 실제 사용자 ID (null이면 미등록 사용자)
     * @param predictedUserId 예측된 사용자 ID (null이면 미식별)
     * @param similarityScore 유사도 점수
     */
    public void recordRecognitionResult(String actualUserId, String predictedUserId, float similarityScore) {
        if (confusionMatrix.isEmpty()) {
            initializeConfusionMatrix();
        }

        // 혼동 행렬 업데이트
        if (actualUserId != null && predictedUserId != null && actualUserId.equals(predictedUserId)) {
            // 올바르게 등록된 사용자를 식별 (TP)
            confusionMatrix.put("TP", confusionMatrix.get("TP") + 1);
        } else if (actualUserId == null && predictedUserId == null) {
            // 올바르게 미등록 사용자를 식별 (TN)
            confusionMatrix.put("TN", confusionMatrix.get("TN") + 1);
        } else if (actualUserId == null && predictedUserId != null) {
            // 미등록 사용자를 등록된 사용자로 잘못 식별 (FP)
            confusionMatrix.put("FP", confusionMatrix.get("FP") + 1);
        } else if (actualUserId != null && (predictedUserId == null || !actualUserId.equals(predictedUserId))) {
            // 등록된 사용자를 식별하지 못하거나 잘못 식별 (FN)
            confusionMatrix.put("FN", confusionMatrix.get("FN") + 1);
        }

        // 사용자별 통계 업데이트
        updateUserStats(actualUserId, predictedUserId, similarityScore);

        // 임계값 최적화 데이터 업데이트
        updateThresholdData(actualUserId, predictedUserId, similarityScore);
    }

    /**
     * 사용자별 인식 통계 업데이트
     */
    private void updateUserStats(String actualUserId, String predictedUserId, float similarityScore) {
        if (actualUserId != null) {
            UserRecognitionStats stats = userStats.computeIfAbsent(actualUserId,
                    k -> new UserRecognitionStats(actualUserId));

            stats.incrementTotalAttempts();

            if (predictedUserId != null && actualUserId.equals(predictedUserId)) {
                stats.incrementSuccessfulRecognitions();
                stats.addSimilarityScore(similarityScore);
            }
        }
    }

    /**
     * 임계값 최적화 데이터 업데이트
     */
    private void updateThresholdData(String actualUserId, String predictedUserId, float similarityScore) {
        // 임계값 범위 (0.5부터 0.95까지 0.05 간격)
        for (float threshold = 0.5f; threshold <= 0.95f; threshold += 0.05f) {
            final float roundedThreshold = Math.round(threshold * 100) / 100.0f;

            boolean wouldBeRecognized = similarityScore >= roundedThreshold;
            boolean isCorrectUser = actualUserId != null && predictedUserId != null && actualUserId.equals(predictedUserId);
            boolean isUnknownUser = actualUserId == null;

            String key = isUnknownUser ? "unknown" : actualUserId;
            Map<Float, ThresholdStats> userThresholdData = thresholdOptimizationData.computeIfAbsent(key,
                    k -> new HashMap<>());

            ThresholdStats stats = userThresholdData.computeIfAbsent(roundedThreshold,
                    t -> new ThresholdStats(roundedThreshold));

            stats.incrementTotal();

            if (isUnknownUser) {
                // 미등록 사용자 케이스
                if (wouldBeRecognized) {
                    stats.incrementFalsePositive();
                } else {
                    stats.incrementTrueNegative();
                }
            } else {
                // 등록된 사용자 케이스
                if (isCorrectUser && wouldBeRecognized) {
                    stats.incrementTruePositive();
                } else if (!isCorrectUser && wouldBeRecognized) {
                    stats.incrementFalsePositive();
                } else if (isCorrectUser && !wouldBeRecognized) {
                    stats.incrementFalseNegative();
                } else {
                    stats.incrementTrueNegative();
                }
            }
        }
    }

    /**
     * 현재 혼동 행렬 상태 조회
     */
    public Map<String, Integer> getConfusionMatrix() {
        return new HashMap<>(confusionMatrix);
    }

    /**
     * 정확도(Accuracy) 계산
     * (TP + TN) / (TP + TN + FP + FN)
     */
    public double calculateAccuracy() {
        int tp = confusionMatrix.getOrDefault("TP", 0);
        int tn = confusionMatrix.getOrDefault("TN", 0);
        int fp = confusionMatrix.getOrDefault("FP", 0);
        int fn = confusionMatrix.getOrDefault("FN", 0);

        int total = tp + tn + fp + fn;

        return total > 0 ? (double) (tp + tn) / total : 0.0;
    }

    /**
     * 정밀도(Precision) 계산
     * TP / (TP + FP)
     */
    public double calculatePrecision() {
        int tp = confusionMatrix.getOrDefault("TP", 0);
        int fp = confusionMatrix.getOrDefault("FP", 0);

        return (tp + fp) > 0 ? (double) tp / (tp + fp) : 0.0;
    }

    /**
     * 재현율(Recall) 계산
     * TP / (TP + FN)
     */
    public double calculateRecall() {
        int tp = confusionMatrix.getOrDefault("TP", 0);
        int fn = confusionMatrix.getOrDefault("FN", 0);

        return (tp + fn) > 0 ? (double) tp / (tp + fn) : 0.0;
    }

    /**
     * F1 점수 계산
     * 2 * (Precision * Recall) / (Precision + Recall)
     */
    public double calculateF1Score() {
        double precision = calculatePrecision();
        double recall = calculateRecall();

        return (precision + recall) > 0 ?
                2 * (precision * recall) / (precision + recall) : 0.0;
    }

    /**
     * 거짓 양성률(False Positive Rate) 계산
     * FP / (FP + TN)
     */
    public double calculateFalsePositiveRate() {
        int fp = confusionMatrix.getOrDefault("FP", 0);
        int tn = confusionMatrix.getOrDefault("TN", 0);

        return (fp + tn) > 0 ? (double) fp / (fp + tn) : 0.0;
    }

    /**
     * 최적 임계값 계산 (F1 점수 기준)
     */
    public Map<String, Float> calculateOptimalThresholds() {
        Map<String, Float> optimalThresholds = new HashMap<>();

        for (Map.Entry<String, Map<Float, ThresholdStats>> entry : thresholdOptimizationData.entrySet()) {
            String userId = entry.getKey();
            Map<Float, ThresholdStats> thresholdData = entry.getValue();

            float bestThreshold = 0.75f; // 기본값
            double bestF1Score = 0.0;

            for (ThresholdStats stats : thresholdData.values()) {
                double precision = stats.calculatePrecision();
                double recall = stats.calculateRecall();
                double f1Score = (precision + recall) > 0 ?
                        2 * (precision * recall) / (precision + recall) : 0.0;

                if (f1Score > bestF1Score) {
                    bestF1Score = f1Score;
                    bestThreshold = stats.getThreshold();
                }
            }

            optimalThresholds.put(userId, bestThreshold);
        }

        return optimalThresholds;
    }

    /**
     * 사용자별 인식 통계 정보 조회
     */
    public List<UserRecognitionStats> getUserStats() {
        return userStats.values().stream()
                .collect(Collectors.toList());
    }

    /**
     * ROC 곡선 데이터 생성
     * 반환 형식: List<Map<String, Object>> - 각 맵은 threshold, tpr, fpr 포함
     */
    public List<Map<String, Object>> generateROCCurveData() {
        // 모든 사용자에 대한 통합 데이터 생성
        Map<Float, ThresholdStats> aggregatedStats = new HashMap<>();

        // 모든 사용자의 데이터 통합
        for (Map<Float, ThresholdStats> userData : thresholdOptimizationData.values()) {
            for (Map.Entry<Float, ThresholdStats> entry : userData.entrySet()) {
                Float threshold = entry.getKey();
                ThresholdStats userStats = entry.getValue();

                ThresholdStats aggregated = aggregatedStats.computeIfAbsent(threshold,
                        t -> new ThresholdStats(threshold));

                aggregated.addStats(userStats);
            }
        }

        // ROC 곡선 데이터 생성
        return aggregatedStats.values().stream()
                .sorted((a, b) -> Float.compare(a.getThreshold(), b.getThreshold()))
                .map(stats -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("threshold", stats.getThreshold());
                    point.put("tpr", stats.calculateRecall()); // True Positive Rate = Recall
                    point.put("fpr", stats.calculateFalsePositiveRate());
                    return point;
                })
                .collect(Collectors.toList());
    }

    /**
     * 평가 결과를 종합적으로 반환
     */
    public Map<String, Object> getEvaluationSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("confusionMatrix", getConfusionMatrix());
        summary.put("metrics", Map.of(
                "accuracy", calculateAccuracy(),
                "precision", calculatePrecision(),
                "recall", calculateRecall(),
                "f1Score", calculateF1Score(),
                "falsePositiveRate", calculateFalsePositiveRate()
        ));
        summary.put("optimalThresholds", calculateOptimalThresholds());
        summary.put("rocCurve", generateROCCurveData());

        return summary;
    }

    /**
     * 현재 통계 초기화
     */
    public void resetStatistics() {
        confusionMatrix.clear();
        userStats.clear();
        thresholdOptimizationData.clear();
        initializeConfusionMatrix();
    }
}

/**
 * 사용자별 인식 통계 정보
 */
@Data
class UserRecognitionStats {
    private final String userId;
    private int totalAttempts = 0;
    private int successfulRecognitions = 0;
    private double averageSimilarityScore = 0.0;
    private double totalSimilarityScore = 0.0;

    public UserRecognitionStats(String userId) {
        this.userId = userId;
    }

    public void incrementTotalAttempts() {
        totalAttempts++;
    }

    public void incrementSuccessfulRecognitions() {
        successfulRecognitions++;
    }

    public void addSimilarityScore(double score) {
        totalSimilarityScore += score;
        averageSimilarityScore = totalSimilarityScore / successfulRecognitions;
    }

    public double getRecognitionRate() {
        return totalAttempts > 0 ? (double) successfulRecognitions / totalAttempts : 0.0;
    }
}

/**
 * 임계값별 성능 통계
 */
@Data
class ThresholdStats {
    private final float threshold;
    private int truePositive = 0;
    private int trueNegative = 0;
    private int falsePositive = 0;
    private int falseNegative = 0;
    private int total = 0;

    public ThresholdStats(float threshold) {
        this.threshold = threshold;
    }

    public void incrementTruePositive() {
        truePositive++;
    }

    public void incrementTrueNegative() {
        trueNegative++;
    }

    public void incrementFalsePositive() {
        falsePositive++;
    }

    public void incrementFalseNegative() {
        falseNegative++;
    }

    public void incrementTotal() {
        total++;
    }

    public void addStats(ThresholdStats other) {
        this.truePositive += other.truePositive;
        this.trueNegative += other.trueNegative;
        this.falsePositive += other.falsePositive;
        this.falseNegative += other.falseNegative;
        this.total += other.total;
    }

    public double calculatePrecision() {
        return (truePositive + falsePositive) > 0 ?
                (double) truePositive / (truePositive + falsePositive) : 0.0;
    }

    public double calculateRecall() {
        return (truePositive + falseNegative) > 0 ?
                (double) truePositive / (truePositive + falseNegative) : 0.0;
    }

    public double calculateFalsePositiveRate() {
        return (falsePositive + trueNegative) > 0 ?
                (double) falsePositive / (falsePositive + trueNegative) : 0.0;
    }
}
