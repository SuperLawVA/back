package com.superlawva.global.response.status;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorStatus {

    // ✅ 공통 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "COMMON408", "요청 시간이 초과되었습니다."),

    // ✅ 회원 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "사용자가 없습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER400", "닉네임은 필수 입니다."),

    // ✅ 게시글 관련
    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE404", "게시글이 없습니다."),

    // ✅ 테스트용
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP400", "이거는 테스트"),

    // ✅ JWT 관련
    INVALID_OR_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT401", "JWT가 유효하지 않거나 만료되었습니다."),

    // ✅ User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "존재하지 않는 사용자입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER409", "이미 사용 중인 이메일입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER401", "비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRM_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER400", "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    USER_ALREADY_SIGNED_UP(HttpStatus.CONFLICT, "U003", "이미 가입된 사용자입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "U005", "유효하지 않은 사용자 ID 형식입니다."),

    // ✅ Email Verification
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL404", "인증 코드를 찾을 수 없습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL400", "인증 코드가 만료되었습니다."),
    VERIFICATION_CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "EMAIL400", "인증 코드가 일치하지 않습니다."),

    // ✅ Kakao
    KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO500", "카카오 서버와의 통신에 실패했습니다."),
    KAKAO_USER_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "KAKAO400", "카카오 사용자 정보를 가져오는 데 실패했습니다. 동의 항목을 확인해주세요."),
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO500", "카카오 토큰 요청에 실패했습니다."),
    KAKAO_USER_INFO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "KAKAO500", "카카오 사용자 정보 조회에 실패했습니다."),

    // ✅ NAVER
    NAVER_TOKEN_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NAVER500", "네이버 토큰 요청에 실패했습니다."),
    NAVER_USER_INFO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NAVER500", "네이버 사용자 정보 조회에 실패했습니다."),

    // ✅ ML
    ML_API_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ML500", "ML 검색 API 호출에 실패했습니다. 관리자에게 문의하세요."),
    ML_API_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "ML408", "ML API 응답 시간이 초과되었습니다."),

    // ✅ MAIL
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL500", "메일 전송에 실패했습니다."),

    // ✅ Document
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DOCUMENT404", "해당 문서를 찾을 수 없습니다."),
    FILE_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DOCUMENT500", "파일 저장에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "DOCUMENT400", "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DOCUMENT500", "파일 업로드에 실패했습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "DOCUMENT400", "파일 크기가 너무 큽니다."),

    // ✅ OCR
    OCR_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR500", "OCR 처리에 실패했습니다."),
    OCR_FILE_EMPTY(HttpStatus.BAD_REQUEST, "OCR400", "업로드된 파일이 비어있습니다."),

    // ✅ Chatbot
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT404", "해당 채팅 세션을 찾을 수 없습니다."),
    FORBIDDEN_ACCESS_TO_SESSION(HttpStatus.FORBIDDEN, "CHAT403", "해당 채팅 세션에 접근할 권한이 없습니다."),
    CHAT_API_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT500", "챗봇 API 호출에 실패했습니다."),

    // ✅ Alarm
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALARM404", "해당 알림을 찾을 수 없습니다."),
    INVALID_ALARM_TYPE(HttpStatus.BAD_REQUEST, "ALARM400", "유효하지 않은 알림 유형입니다."),

    // ✅ Contract
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTRACT404", "해당 계약서를 찾을 수 없습니다."),
    CONTRACT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTRACT500", "계약서 생성에 실패했습니다."),

    // ✅ Search
    SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEARCH500", "검색 처리에 실패했습니다."),
    SEARCH_QUERY_EMPTY(HttpStatus.BAD_REQUEST, "SEARCH400", "검색어를 입력해주세요."),

    // 인증 관련 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),

    // 일반 에러
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB001", "데이터베이스 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
