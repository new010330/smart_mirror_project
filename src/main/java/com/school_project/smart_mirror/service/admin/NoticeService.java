package com.school_project.smart_mirror.service.admin;

import com.school_project.smart_mirror.domain.admin.Notice;
import com.school_project.smart_mirror.dto.admin.NotificationRequestDto;
import com.school_project.smart_mirror.dto.admin.NotificationRespDto;
import com.school_project.smart_mirror.repository.admin.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

    public NotificationRespDto getNotifications(LocalDate cur_date) {
        Notice notice = noticeRepository.findAllByNotifications(cur_date);

        log.info("공백 체크: " + notice.isEmpty());
//        log.info(notice.getTitle());
//        log.info(NotificationRespDto.builder().title(notice.getTitle()).toString());


        return NotificationRespDto.builder()
                .title(notice.getTitle())
                .content(notice.getContent())
                .locationName(notice.getLocation_name())
                .startDate(notice.getStart_date())
                .endDate(notice.getEnd_date())
                .build();

    }

}
