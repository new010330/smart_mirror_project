package com.school_project.smart_mirror.service.faceApi;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school_project.smart_mirror.domain.faceApi.Face;
import com.school_project.smart_mirror.repository.faceApi.FaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FaceSearchService {
    private final ElasticsearchClient esClient;
    private final FaceRepository faceRepository;
    private final float similarityThreshold = 0.97f;  // 임계값 설정 (0.0-1.0 사이 값)

    public Face searchSimilarFaces(List<Float> queryVector, long k) throws Exception {

        // 시작 시간 측정
        long startTime = System.currentTimeMillis();
        try {
            SearchResponse<Face> response = esClient.search(s -> s
                            .index("users")
                            .knn(knn -> knn
                                    .field("faceEmbedding")
                                    .queryVector(queryVector)
                                    .k(k)  // 여러 후보를 검색하되
                                    .numCandidates(100L)
                            ),
                    Face.class
            );

            // 유사도 필터 적용 후 가장 높은 유사도를 가진 얼굴만 선택
            Optional<Hit<Face>> results = response.hits().hits().stream()
                    .filter(hit -> hit.score() >= similarityThreshold)
                    .max(Comparator.comparing(Hit::score));

//            List<Face> results = response.hits().hits().stream()
//                    .map(hit -> hit.source())
//                    .collect(Collectors.toList());

            log.info("result 개수: " + response.hits().total());
            log.info("result 점수: " + response.hits().maxScore());
            log.info("result체크: " + results.toString());

            if (results.isPresent()) {
                // 일치하는 얼굴 발견, 기존 사용자 처리
                Face user = results.get().source();
                log.info("유사한 얼굴을 찾았습니다. 사용자 ID: {}, 유사도: {}\n\n",
                        user.getId(), results.get().score());

                // 종료 시간 측정 및 소요 시간 계산
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                log.info("(조회)얼굴 인식 처리 시간: {}ms\n\n", executionTime);
                return user;
            } else {
                // 일치하는 얼굴 없음, 새 사용자 등록
                log.info("유사한 얼굴을 찾을 수 없어 새로 등록합니다.\n\n");
                String newUserId = UUID.randomUUID().toString();
                Face newFace = registerFace(newUserId, queryVector);

                // 종료 시간 측정 및 소요 시간 계산
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                log.info("(등록)얼굴 인식 처리 시간: {}ms", executionTime);
                return newFace;
            }

//            // 결과가 없으면 자동 등록
//            if (results.isEmpty()) {
//                log.info("유사한 얼굴을 찾을 수 없어 새로 등록합니다.");
//                String newUserId = UUID.randomUUID().toString();
//                Face newFace = registerFace(newUserId, queryVector);
//                results.add(newFace);
//                log.info("새 사용자 등록 완료: {}", newUserId);
//            } else {
//                log.info("유사한 얼굴을 찾았습니다. 검색 결과 수: {}\n\n", results.size());
//            }
//
//            return results;
        } catch (Exception e) {
            log.error("벡터 검색 또는 등록 실패", e);
            throw new RuntimeException("벡터 검색 실패", e);
        }
    }

    public Face registerFace(String userId, List<Float> faceEmbedding) {
        try {
            Face face = new Face();
            face.setId(userId != null ? userId : UUID.randomUUID().toString());
            face.setFaceEmbedding(faceEmbedding);

            // ElasticSearch에 직접 저장
            IndexResponse response = esClient.index(i -> i
                    .index("users")
                    .id(face.getId())
                    .document(face)
            );

            log.info("얼굴 벡터 저장 완료: userId={}, indexedId={}", face.getId(), response.id());
            return face;
        } catch (Exception e) {
            log.error("얼굴 등록 실패", e);
            throw new RuntimeException("얼굴 등록 실패", e);
        }
    }

}
