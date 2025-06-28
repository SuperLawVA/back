package com.superlawva.global.response.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus {

    _OK(HttpStatus.OK, "200", "OK"),
    _CREATED(HttpStatus.CREATED, "201", "CREATED"),
    
    // 알람 관련 성공 상태
    ALARM_LIST_RETRIEVED(HttpStatus.OK, "200", "알람 목록을 성공적으로 조회했습니다."),
    ALARM_STATS_RETRIEVED(HttpStatus.OK, "200", "알람 통계를 성공적으로 조회했습니다."),
    ALARM_MARKED_AS_READ(HttpStatus.OK, "200", "알림을 읽음 처리했습니다."),
    ALARM_DELETED(HttpStatus.OK, "200", "알림이 삭제되었습니다."),
    ALARM_CREATED(HttpStatus.CREATED, "201", "알림이 생성되었습니다."),
    
    // 계약서 관련 성공 상태
    CONTRACT_CREATED(HttpStatus.CREATED, "201", "계약서가 성공적으로 생성되었습니다."),
    CONTRACT_RETRIEVED(HttpStatus.OK, "200", "계약서를 성공적으로 조회했습니다."),
    CONTRACT_UPDATED(HttpStatus.OK, "200", "계약서가 성공적으로 수정되었습니다."),
    CONTRACT_DELETED(HttpStatus.OK, "200", "계약서가 성공적으로 삭제되었습니다."),
    CONTRACT_LIST_RETRIEVED(HttpStatus.OK, "200", "계약서 목록을 성공적으로 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

} 