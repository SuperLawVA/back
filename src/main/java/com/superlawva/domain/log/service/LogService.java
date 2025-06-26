package com.superlawva.domain.log.service;

import com.superlawva.domain.log.dto.*;
import com.superlawva.domain.log.dto.EventRequestDTO;
import com.superlawva.domain.log.dto.PageViewRequestDTO;
import com.superlawva.domain.log.dto.SessionRequestDTO;
import com.superlawva.domain.log.entity.*;
import com.superlawva.domain.log.repository.*;
import com.superlawva.domain.user.entity.User;
import com.superlawva.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    /* ------------- Repositories ------------- */
    private final SessionRepository sessionRepo;
    private final PageViewRepository viewRepo;
    private final EventRepository eventRepo;

    private final ClickLogRepository clickRepo;
    private final ErrorLogRepository errorRepo;
    private final SearchResultRepository searchResultRepo;
    // Hover / Scroll … 필요시 추가

    private final UserRepository     userRepo;   // 🔑 User 엔티티 프록시용

    /* -------- 세션 -------- */
    @Transactional
    public Long handleSession(SessionRequestDTO req) {

        if ("start".equals(req.action())) {
            Session s = new Session();
            s.setStartedAt(LocalDateTime.now());
            s.setDevice(req.device());

            // ✅ 기존 사용자 프록시 주입
            if (req.userId() != null) {
                s.setUser(userRepo.getReferenceById(req.userId()));
            }

            return sessionRepo.save(s).getId();
        }

        // -------- end --------
        Session s = sessionRepo.findById(req.sessionId())
                .orElseThrow();          // TODO: custom 예외로 교체
        s.setEndAt(LocalDateTime.now());
        return s.getId();
    }

    /* -------- 페이지뷰 -------- */
    @Transactional
    public Long handlePageView(PageViewRequestDTO req) {

        if ("start".equals(req.action())) {
            PageView v = new PageView();
            v.setSession(sessionRepo.getReferenceById(req.sessionId()));
            v.setPath(req.path());
            v.setStartedAt(LocalDateTime.now());
            return viewRepo.save(v).getId();
        }

        // -------- end --------
        PageView v = viewRepo.findById(req.viewId())
                .orElseThrow();
        int duration = (int) ChronoUnit.MILLIS.between(
                v.getStartedAt(), LocalDateTime.now());
        v.setDuration(duration);
        return v.getId();
    }

    /* -------- 이벤트 + 세부 로그 -------- */
    @Transactional
    public Long handleEvent(EventRequestDTO req) {

        Event e = new Event();
        e.setType(req.type());
        e.setTarget(req.target());
        e.setTime(req.time());
        e.setSession(sessionRepo.getReferenceById(req.sessionId()));
        e.setView(viewRepo.getReferenceById(req.viewId()));

        if (req.userId() != null) {
            e.setUser(userRepo.getReferenceById(req.userId()));
        }

        eventRepo.save(e);

        /* --- 타입별 서브 로그 분기 --- */
        switch (req.type()) {
            case "click"  -> saveClickLog(e, req.meta());
            case "error"  -> saveErrorLog(e, req.meta());
            // case "hover" -> saveHoverLog(…);
            // case "form"  -> …
            default       -> { /* no-op or throw */ }
        }
        return e.getId();
    }

    /* ---- click_log ---- */
    private void saveClickLog(Event e, Map<String, Object> m) {
        ClickLog c = new ClickLog();
        c.setEvent(e);
        c.setX(((Number) m.get("x")).intValue());
        c.setY(((Number) m.get("y")).intValue());
        c.setClickCount(((Number) m.get("click_count")).intValue());
        c.setIntervalAvg(((Number) m.get("interval_avg")).intValue());
        c.setElement((String) m.get("element"));
        clickRepo.save(c);
    }

    /* ---- error_log ---- */
    private void saveErrorLog(Event e, Map<String, Object> m) {
        ErrorLog err = new ErrorLog();
        err.setEvent(e);
        err.setMessage((String) m.get("message"));
        err.setPath((String) m.get("path"));
        errorRepo.save(err);
    }

    /**
     * 검색 로그 기록
     * @param user 검색을 수행한 사용자
     * @param query 검색어
     * @param resultCount 검색 결과 개수
     */
    @Transactional
    public void logSearch(User user, String query, int resultCount) {
        /*
        // TODO: SearchResult 엔티티 구조 변경에 따라 로깅 로직 재설계 필요
        SearchResult searchLog = new SearchResult(null, user, query, resultCount, LocalDateTime.now());
        searchResultRepo.save(searchLog);
        log.info("검색 로그 기록 - 사용자: {}, 검색어: '{}', 결과: {}개", user.getId(), query, resultCount);
        */
    }
}
