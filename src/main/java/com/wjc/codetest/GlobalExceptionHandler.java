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

/*
문제: Exception 부족 -> 보완 필요
원인: Exception을 잡고 있지 않는 것들이 있어 의도한 메시지를 출력하지 않고 Spring에서 자동으로 처리함.
     -> 응답 메시지 형식 불일치
개선안(추가할 내용):
  - MethodArgumentNotValidException           (Request Body의 @Valid 검증)
  - ConstraintViolationException              (Request Param / PathVariable의 @Valid 검증)
  - MethodArgumentTypeMismatchException       (잘못된 파라미터 사용 시)
  - MissingRequestCookieException             (쿠키를 사용한다면)
  - MissingServletRequestParameterException   (빠진 파라미터가 있는 경우)
  - HttpMessageNotReadableException           (Enum 타입에서 벗어났을 때)
  - DataIntegrityViolationException           (DB 제약조건 위반)
*/
