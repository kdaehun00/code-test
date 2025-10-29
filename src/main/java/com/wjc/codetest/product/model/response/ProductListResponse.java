package com.wjc.codetest.product.model.response;

import com.wjc.codetest.product.model.domain.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author : 변영우 byw1666@wjcompass.com
 * @since : 2025-10-27
 */
@Getter
@Setter
public class ProductListResponse {
    private List<Product> products;
    private int totalPages;
    private long totalElements;
    /*
    문제: FE에서 데이터를 이어받아 무한 스크롤 또는 페이지네이션 UI를 처리하기 어려움
    원인: 마지막 페이지인지에 대한 여부가 없음
    개선안: isLast라는 boolean 타입을 선언하며 마지막 페이지인지에 대한 여부를 FE 측에 전달함.
    */
    private int page;

    /*
    문제: Spring Data의 Page 객체를 활용하지 않고, 페이지네이션을 수동으로 처리하여 중복 코드 발생

    원인:
      - Page 객체의 기능을 활용하지 않음
      - 페이지네이션 로직을 매번 수동으로 작성해야 함

    개선안:
      - Spring Data의 Page 인터페이스 직접 반환
        (ex - ProductListResponse(Page<Product> page) 와 같은 형식으로 작성
    */
    public ProductListResponse(List<Product> content, int totalPages, long totalElements, int number) {
        this.products = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.page = number;
    }
}
