package com.school_project.smart_mirror.service.admin;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.school_project.smart_mirror.domain.admin.Mirror;
import com.school_project.smart_mirror.domain.admin.User;
import com.school_project.smart_mirror.dto.admin.CreateMirrorRequestDto;
import com.school_project.smart_mirror.dto.admin.LoginRequestDto;
import com.school_project.smart_mirror.dto.admin.UserInfo;
import com.school_project.smart_mirror.exception.CustomValidationApiException;
import com.school_project.smart_mirror.repository.admin.MirrorRepository;
import com.school_project.smart_mirror.repository.admin.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

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
    public boolean addUser(CreateMirrorRequestDto mirrorRequestDto) {
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

    public UserInfo login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("사용자를 찾을 수 없습니다.") {});
        log.info("userGET: " + user.getMirror_id());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.") {};
        }

        Mirror mirror = mirrorRepository.findById(user.getMirror_id())
                .orElseThrow(() -> new AuthenticationException("Mirror 정보를 찾을 수 없습니다.") {});

        log.info("location_name: " + mirror.getLocation_name());

        // 3. 로그인 성공 시 사용자 정보 반환
        return UserInfo.builder()
                .locationName(mirror.getLocation_name())
                .latitude(mirror.getLatitude())
                .longitude(mirror.getLongitude())
                .features(mirror.getFeatures())
                .username(user.getUsername())
                .build();
    }

}
