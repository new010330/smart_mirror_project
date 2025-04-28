package com.school_project.smart_mirror.repository.faceApi;

import com.school_project.smart_mirror.domain.faceApi.Face;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceRepository extends ElasticsearchRepository<Face, String> {
    // 커스텀 쿼리 메서드 추가 가능
    List<Face> findByFaceEmbedding(String vector) throws Exception;
}