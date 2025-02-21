package com.school_project.smart_mirror.controller.admin;

import com.school_project.smart_mirror.dto.CMRespDto;
import com.school_project.smart_mirror.dto.admin.NotificationRequestDto;
import com.school_project.smart_mirror.service.admin.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NoticeService noticeService;


    @PostMapping("/notification")
    public ResponseEntity<?> addNotification(@RequestBody NotificationRequestDto notificationRequestDto) {
        log.info("controller check");
        boolean status = noticeService.addNotice(notificationRequestDto);

        return ResponseEntity.ok().body(new CMRespDto<>(201, "success", status));
    }

}
