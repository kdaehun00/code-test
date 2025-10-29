package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
