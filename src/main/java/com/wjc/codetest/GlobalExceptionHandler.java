package com.wjc.codetest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice(value = {"com.wjc.codetest.product.controller"})
public class GlobalExceptionHandler {

    /*
    문제:
      - 예외 처리가 RuntimeException 하나로 통합되어 에러 타입 구분 불가
      - 예외 발생 시 공통된 응답 구조가 없어 FE와의 연동 시 일관성 저하
      - 성공/실패 응답이 통일되지 않아 유지보수 및 확장성 낮음
    원인:
      - 공통 응답 객체(BaseApiResponse)가 정의되어 있지 않음
      - 예외 처리 시 RuntimeException 등 단순 예외만 반환하여 메시지 구조 불명확
    개선안:
      - BaseApiResponse 클래스를 추가하고 `success()` / `error()` 정적 메서드 제공
      - 모든 컨트롤러가 BaseApiResponse를 사용하도록 표준화
      - GlobalExceptionHandler에서 BaseApiResponse.error()로 일관된 에러 응답 생성
      - 도메인별 CustomException + ErrorCode Enum 설계로 세분화된 예외 관리
    효과:
      - FE와 BE 간 응답 포맷 일관성 확보
      - API 응답 구조 단일화로 유지보수성 향상
      - 전역 예외 처리 로직 단순화 및 로깅 효율성 증가
    */
    @ResponseBody
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> runTimeException(Exception e) {
        log.error("status :: {}, errorType :: {}, errorCause :: {}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "runtimeException",
                e.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
