package com.school_project.smart_mirror.controller.admin;

import com.school_project.smart_mirror.dto.CMRespDto;
import com.school_project.smart_mirror.dto.admin.CreateMirrorRequestDto;
import com.school_project.smart_mirror.dto.admin.LoginRequestDto;
import com.school_project.smart_mirror.dto.admin.UserInfo;
import com.school_project.smart_mirror.service.admin.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/users/register")
    public ResponseEntity<?> addUser(@RequestBody @Valid CreateMirrorRequestDto mirrorRequestDto) {
        boolean status = userService.addUser(mirrorRequestDto);


        return ResponseEntity.ok().body(new CMRespDto<>(201, "success", mirrorRequestDto));
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto request) {
        log.info("check");
        try {
            UserInfo userInfo = userService.login(request);
            return ResponseEntity.ok(new CMRespDto<>(1, "로그인 성공", userInfo));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CMRespDto<>(-1, "아이디 또는 비밀번호가 올바르지 않습니다.", "null"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CMRespDto<>(-1, "서버 오류가 발생했습니다.", "null"));
        }
    }

}
