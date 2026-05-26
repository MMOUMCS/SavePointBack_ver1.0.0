package com.couple.gallery.couple_gallery_backend.exception;

import com.couple.gallery.couple_gallery_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException; // 👈 누락된 Import 추가
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiException {

    // 1. 중복 이메일 등의 비즈니스 로직 오류 처리 (IllegalStateException으로 통합)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        String message = e.getMessage();

        // 이메일 중복 오류 식별 (UserService에서 "DUPLICATE_EMAIL:" 접두사를 던졌다고 가정)
        if (message != null && message.startsWith("DUPLICATE_EMAIL:")) {
            ErrorResponse response = ErrorResponse.builder()
                    .errorCode("DUPLICATE_EMAIL")
                    .message("이미 존재하는 이메일입니다.")
                    .build();
            return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409
        }

        // 그 외의 IllegalStateException (400 Bad Request)
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("BAD_REQUEST_STATE")
                .message("잘못된 요청 상태입니다.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        // HTTP 401 Unauthorized 반환
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("AUTH_FAILED") // 인증 실패 코드
                // 사용자에게는 상세 내용 대신 일반적인 실패 메시지 전달
                .message("자격 증명에 실패하였습니다.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 👈 401 반환
    }


    // 2. 입력값 검증 실패 예외 처리 (@Size, @Email 오류 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // HTTP 400 Bad Request 반환
        String defaultMessage = e.getBindingResult().getFieldError().getDefaultMessage();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_FAILED")
                .message(defaultMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // 3. 모든 예상치 못한 예외 처리 (최후의 방어선)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        // 실제 운영 환경에서는 e.printStackTrace() 대신 로그 시스템을 사용해야 합니다.
        e.printStackTrace();

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("처리 중 예상치 못한 서버 오류가 발생했습니다. (관리자 문의)")
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }
}