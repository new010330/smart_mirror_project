package com.school_project.smart_mirror.service.admin;

import com.school_project.smart_mirror.domain.admin.Notice;
import com.school_project.smart_mirror.dto.admin.notification.NotificationRequestDto;
import com.school_project.smart_mirror.dto.admin.notification.NotificationRespDto;
import com.school_project.smart_mirror.repository.admin.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;

    public boolean addNotice(NotificationRequestDto request) {
        try {
            Notice notice = new Notice();

            notice.setTitle(request.getTitle());
            notice.setContent(request.getContent());
            notice.setLocation_name(request.getLocationName());
            notice.setStart_date(request.getStartDate());
            notice.setEnd_date(request.getEndDate());

            noticeRepository.save(notice);
            log.info("check 완료");

            return true;

        } catch (Exception e) {
            log.error("공지사항 저장 실패: {}", e.getMessage(), e);  // 로깅 추가
            return false;
        }
    }

    public List<NotificationRespDto> getNotifications() {
        LocalDate today = LocalDate.now();
        List<Notice> notice = noticeRepository.findAllByNotifications(today);

        return notice.stream()
                .map(NotificationRespDto::noticeToDto)  // 정적 팩토리 메서드 활용
                .collect(Collectors.toList());

    }

}
