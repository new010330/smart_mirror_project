package com.school_project.smart_mirror.controller.faceApi;

import com.school_project.smart_mirror.domain.faceApi.Face;
import com.school_project.smart_mirror.dto.CMRespDto;
import com.school_project.smart_mirror.dto.admin.FaceSearchRequestDto;
import com.school_project.smart_mirror.repository.faceApi.FaceRepository;
import com.school_project.smart_mirror.service.faceApi.FaceSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FaceController {
    private final FaceSearchService faceSearchService;

//    @PostMapping("/homes/usercheck")
//    @CrossOrigin(origins = "*", methods = RequestMethod.POST)
//    public ResponseEntity<?> searchSimilarFaces(@RequestBody Map<Integer, Float> request) throws Exception {
//        log.info("컨트롤러 Test Check");
//
//        List<Float> parseVector = new ArrayList<>();
//        for(int i = 0; i < 128; i++) {
//            parseVector.add(request.get(i));
//        }
//        FaceSearchRequestDto requestDto = new FaceSearchRequestDto();
//        requestDto.setVector(parseVector);
//
//        List<Face> list = faceSearchService.searchSimilarFaces(requestDto.getVector(), 5);
//
//        for(int i = 0; i < list.size(); i++) {
//            log.info(list.toString());
//        }
//
//        return ResponseEntity.ok().body(new CMRespDto<>(1, "데이터 추출", list));
//    }


    @PostMapping("/homes/usercheck")
    @CrossOrigin(origins = "*", methods = RequestMethod.POST)
    public ResponseEntity<?> searchSimilarFaces(@RequestBody Map<Integer, Float> request) {
        try {
            log.info("얼굴 검색 요청 수신");

            // 벡터 데이터 추출
            List<Float> parseVector = new ArrayList<>();
            for(int i = 0; i < 128; i++) {
                Float value = request.get(i);
                if (value == null) {
                    return ResponseEntity.badRequest().body(new CMRespDto<>(-1, "벡터 데이터 형식이 올바르지 않습니다.", null));
                }
                parseVector.add(value);
            }

            // 유사 얼굴 검색
            Face similarFaces = faceSearchService.searchSimilarFaces(parseVector, 5L);

            return ResponseEntity.ok().body(new CMRespDto<>(1, "검색 완료", similarFaces));
        } catch (Exception e) {
            log.error("얼굴 검색 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(new CMRespDto<>(-1, "처리 중 오류 발생: " + e.getMessage(), null));
        }
    }
}
