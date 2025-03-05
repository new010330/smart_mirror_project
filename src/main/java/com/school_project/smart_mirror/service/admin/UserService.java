package com.school_project.smart_mirror.service.admin;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.school_project.smart_mirror.domain.admin.Mirror;
import com.school_project.smart_mirror.domain.admin.User;
import com.school_project.smart_mirror.dto.admin.UserInfoRequestDto;
import com.school_project.smart_mirror.dto.admin.UserAuthRequestDto;
import com.school_project.smart_mirror.dto.admin.UserInfoRespDto;
import com.school_project.smart_mirror.exception.CustomValidationApiException;
import com.school_project.smart_mirror.repository.admin.MirrorRepository;
import com.school_project.smart_mirror.repository.admin.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final MirrorRepository mirrorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean addUser(UserInfoRequestDto mirrorRequestDto) {
        try {
            // 중복 회원 검증
            if (userRepository.existsByUsername(mirrorRequestDto.getUsername())) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put("username", "이미 존재하는 사용자명입니다.");
                throw new CustomValidationApiException("중복된 사용자", errorMap);
            }

            User user = new User();
            String encodedPassword = passwordEncoder.encode(mirrorRequestDto.getPassword());
            user.setUsername(mirrorRequestDto.getUsername());
            user.setPassword(encodedPassword);

            Mirror mirror = new Mirror();

            mirror.setLocation_name(mirrorRequestDto.getLocationName());
            mirror.setLatitude(mirrorRequestDto.getLatitude());
            mirror.setLongitude(mirrorRequestDto.getLongitude());
            mirror.setFeatures(mirrorRequestDto.getFeatures());

            userRepository.save(user);
            mirrorRepository.save(mirror);

            return true;

        } catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반 시
            throw new CustomValidationApiException("데이터베이스 오류");
        } catch (Exception e) {
            log.error("회원가입 실패 - 사용자: {}, 원인: {}",
                    mirrorRequestDto.getUsername(),
                    e.getMessage());
            throw new CustomValidationApiException("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    public UserInfoRespDto login(UserAuthRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomValidationApiException("사용자를 찾을 수 없습니다.") {});
        log.info("userGET: " + user.getMirror_id());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomValidationApiException("비밀번호가 일치하지 않습니다.") {};
        }

        Mirror mirror = mirrorRepository.findById(user.getMirror_id())
                .orElseThrow(() -> new CustomValidationApiException("Mirror 정보를 찾을 수 없습니다.") {});

        log.info("location_name: " + mirror.getLocation_name());

        // 3. 로그인 성공 시 사용자 정보 반환
        return UserInfoRespDto.builder()
                .locationName(mirror.getLocation_name())
                .latitude(mirror.getLatitude())
                .longitude(mirror.getLongitude())
                .features(mirror.getFeatures())
                .username(user.getUsername())
                .build();
    }

    public UserInfoRespDto getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomValidationApiException("사용자를 찾을 수 없습니다.") {});

        Mirror mirror = mirrorRepository.findById(user.getMirror_id())
                .orElseThrow(() -> new CustomValidationApiException("미러 정보를 찾을 수 없습니다.") {});

        return UserInfoRespDto.builder()
                .locationName(mirror.getLocation_name())
                .latitude(mirror.getLatitude())
                .longitude(mirror.getLongitude())
                .features(mirror.getFeatures())
                .username(user.getUsername())
                .build();
    }

    public boolean updateUserInfo(UserInfoRequestDto userInfoReqDto) {
        try {
            User user = userRepository.findByUsername(userInfoReqDto.getUsername())
                    .orElseThrow(() -> new CustomValidationApiException("사용자를 찾을 수 없습니다.") {});

            Mirror mirror = mirrorRepository.findById(user.getMirror_id())
                    .orElseThrow(() -> new CustomValidationApiException("미러 정보를 찾을 수 없습니다.") {});

            // 변경할 필드가 null이 아닌 경우에만 업데이트
            if (userInfoReqDto.getLocationName() != null) {
                mirror.setLocation_name(userInfoReqDto.getLocationName());
            }
            if (userInfoReqDto.getLatitude() != null) {
                mirror.setLatitude(userInfoReqDto.getLatitude());
            }
            if (userInfoReqDto.getLongitude() != null) {
                mirror.setLongitude(userInfoReqDto.getLongitude());
            }
            if (userInfoReqDto.getFeatures() != null) {
                mirror.setFeatures(userInfoReqDto.getFeatures());
            }

            mirror.setUpdated_at(LocalDate.now());

            // 변경 사항 저장
            mirrorRepository.save(mirror);

            return true;
        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteUser(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new CustomValidationApiException("사용자를 찾을 수 없습니다."));

            log.info("사용자 미러 ID: " + user.getMirror_id());

            mirrorRepository.deleteById(user.getMirror_id());

            userRepository.deleteById(user.getMirror_id());

            return true;
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류 발생: " + e.getMessage(), e);
            return false;
        }
    }



}
