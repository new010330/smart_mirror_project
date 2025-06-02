package com.school_project.smart_mirror.controller.admin;

import com.school_project.smart_mirror.dto.CMRespDto;
import com.school_project.smart_mirror.dto.admin.auth.LoginRespDto;
import com.school_project.smart_mirror.dto.admin.auth.UserInfoRequestDto;
import com.school_project.smart_mirror.dto.admin.auth.UserAuthRequestDto;
import com.school_project.smart_mirror.dto.admin.auth.UserInfoRespDto;
import com.school_project.smart_mirror.service.admin.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    @GetMapping("/usercheck")
    public ResponseEntity<String> getCheckUser() {
        log.info("check");
        return ResponseEntity.ok("관리자 페이지 접속");
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> addUser(@RequestBody @Valid UserInfoRequestDto mirrorRequestDto) {
        boolean status = userService.addUser(mirrorRequestDto);


        return ResponseEntity.ok().body(new CMRespDto<>(201, "success", mirrorRequestDto));
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserAuthRequestDto request) {
        log.info("check");
        try {
            LoginRespDto tokens = userService.login(request);
            log.info("토큰: " + tokens.toString());
            return ResponseEntity.ok().body(new CMRespDto<>(1, "로그인 성공", tokens));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new CMRespDto<>(-1, e.getMessage(), null));
        }
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUserInfo(@PathVariable("username") String username) {
        try {
            UserInfoRespDto userInfo = userService.getUserInfo(username);
            return ResponseEntity.ok().body(new CMRespDto<>(1, "미러 데이터 조회 성공", userInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new CMRespDto<>(-1, e.getMessage(), null));
        }
    }

    @PutMapping("/users")
    public ResponseEntity<?> editUserInfo(@RequestBody UserInfoRequestDto userInfoReqDto) {
        boolean status = userService.updateUserInfo(userInfoReqDto);

        return ResponseEntity.ok().body(new CMRespDto<>(1, "미러 업데이트 성공", status));
    }

    @DeleteMapping("users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable("username") String username) {
        boolean status = userService.deleteUser(username);

        return ResponseEntity.ok().body(new CMRespDto<>(1, "미러 삭제 성공", 0));
    }


}
