package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
문제: 페이지네이션 파라미터를 Request 클래스로 받고 있어 불필요한 객체 생성 발생.

원인:
  1. 쿼리 파라미터(category, page, size)를 Request 객체로 래핑
  2. 페이지네이션은 URL 파라미터로 전달하는 것이 RESTFul 함. (Get에는 보통 requestbody를 사용하지 않기 때문)

개선안:
  1. @RequestParam으로 쿼리 파라미터 직접 받기
  2. Pageable 인터페이스 사용

고려할 점:
  - 2014년에 최신화된 RPC 문서에는 GET에 대한 body 사용 금지 부분을 삭제하긴 했음.
  - 이 부분은 회사 개발자간 약속에 따라 사용하는 것이 바람직해보임.
*/
public class GetProductListRequest {
    private String category;
    private int page;
    private int size;
}

/*
문제: GitHub 푸시 시 NewLine 관련 에러 발생 우려
원인: 파일 끝에 개행 문자 미삽입
개선안: IDE 설정을 통해 저장 시 자동으로 개행 문자 삽입
*/
