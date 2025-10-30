package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/*
문제: API 문서화 미흡
원인:
  - 컨트롤러 메서드에 Swagger/OpenAPI 어노테이션 부재
  - 엔드포인트, 파라미터, 응답 구조가 자동 문서화되지 않아 FE 개발자나 외부 사용자에게 API 명세 제공 어려움
개선안:
  - 각 컨트롤러 메서드에 @Operation, @Parameter, @ApiResponse 등 OpenAPI 어노테이션 추가
  - 전체 API는 @Tag를 사용해 그룹화
효과:
  - 자동화된 API 문서 생성 가능
  - FE/BE 협업 및 외부 사용자에게 명확한 API 명세 제공
  - 유지보수 편리
*/
@RestController
/*
문제: 엔드포인트의 리소스 구분이 불명확함
원인:
  - @RequestMapping에 공통 prefix 미지정
  - 각 메서드마다 "/product" 경로를 반복적으로 선언함
개선안:
  - 클래스 단위 @RequestMapping("/product") 추가
효과:
  - URL 구조 일관성 확보
  - 가독성 향상 및 유지보수 용이
*/

/*
문제: API 버저닝 미적용으로, 향후 스펙 변경 시 하위 호환성 유지 어려움
원인:
  - @RequestMapping에 버전 정보 미포함
  - 동일 URI로 여러 버전의 API가 공존할 경우 충돌 위험 존재
개선안:
  - @RequestMapping("/api/v1/product") 형식으로 버전 및 리소스 명시
  - 이후 변경 시 /api/v2/... 형태로 확장 가능
효과:
  - API 버전별 관리 용이
  - 유지보수성 및 확장성 향상
*/
@RequestMapping
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    /*
    문제: API가 RESTFul 하지 않음.
    원인: RESTful API 네이밍 규칙을 따르지 않고 get 키워드를 URL에 포함함.
    개선안: URL에서 동사를 제거하고 리소스 명사 중심으로 변경
          (ex - /products/{productId})
    효과:
      - RESTful 규칙 준수
      - 일관된 API 설계
    */
    @GetMapping(value = "/get/product/by/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId){
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /*
    문제: 새로운 자원이 생성되었음을 명확히 표현하지 못함
    원인: HTTP 상태 코드 의미 미고려
    개선안:
      - 201 Created 사용하여 자원 생성 의도 명확히 표현
      - Location 헤더에 생성된 자원의 URI 포함
    효과:
      - 클라이언트가 생성된 자원의 위치를 즉시 파악 가능
      - API 명확성 및 표준성 향상
    */

    /*
    문제:
      - CreateProductRequest, UpdateProductRequest의 필드 유효성 검사 없음
      - null이나 유효하지 않은 데이터가 서비스 계층에 전달될 수 있음
      - 비즈니스 로직 전에 데이터 검증이 이루어지지 않음
    원인:
      - @Valid 어노테이션 미사용
    개선안:
      - DTO에 검증 어노테이션 적용 후 Controller 내 인자에서 @Valid 적용
    효과:
      - 잘못된 데이터에 대해 조기 에러 반환
      - 서비스 계층의 부담 감소
    */
    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /*
    문제: RESTful 규칙 위반으로 인한 동작 혼란
    원인: 리소스 삭제 동작이지만 PostMapping 사용
    개선안: DeleteMapping으로 변경, delete 제거
    */

    /*
    문제: 삭제 또는 업데이트 응답으로 단순 boolean(true) 반환
    원인: 현재 구현에서 update/delete 동작 후 엔티티나 DTO를 반환하지 않고 단순 boolean만 반환
    개선안:
      - 반환할 데이터가 있는 경우: 200 OK / 반환할 데이터가 없는 경우 (현재): 204 No Content 권장
    효과:
      - 클라이언트에서 변경된 상태를 명확히 확인 가능
      - API 응답 일관성
    */
    @PostMapping(value = "/delete/product/{productId}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

    /*
    문제:
      - 엔티티 객체(Product)를 그대로 API 응답으로 반환하고 있음
      - JPA 엔티티의 내부 구조가 그대로 직렬화되어 노출될 가능성 존재
      - 엔티티 변경 시 API 스펙이 의도치 않게 변동될 수 있음
    원인:
      - 서비스 계층의 반환 객체를 그대로 Controller 응답에 사용함
      - DTO 변환 계층(응답 전용 Response DTO) 부재
    개선안:
      - 응답 전용 DTO 클래스 생성 후 변환하여 반환
      - 도메인 엔티티는 영속성 관리 및 비즈니스 로직 전용으로만 사용
    효과:
      - 데이터 노출 범위 제어로 보안성 향상
      - API 응답 구조의 일관성 유지
      - 엔티티 변경이 외부 스펙에 영향을 주지 않아 유지보수성 개선
    */

    /*
    문제:
      - RESTful 규칙 위반 - 리소스 부분 수정에 PostMapping 사용
      - POST는 멱등하지 않아 같은 요청을 여러 번 보내면 예측 불가능한 결과 발생 가능
    원인:
      - 부분 업데이트 시 적절한 HTTP 메서드 미사용
    개선안:
      - PatchMapping으로 변경하여 부분 업데이트 의도를 명확히 표현
    효과:
      - 클라이언트와 서버 간 의도 명확화
      - 멱등성 보장
    생각해볼 점:
      - PUT vs PATCH
        -> PUT: 전체 리소스 교체 (멱등함, 누락된 필드는 null로 설정, 기존 리소스가 없는 경우 새로 생성)
        -> PATCH: 부분 리소스 수정 (멱등할 수도 있고 아닐 수도 있다, 명시된 필드만 수정)
        -> 현재 UpdateProductRequest는 일부 필드만 받으므로 PATCH 사용 권장
    */
    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto){
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }

    /*
    문제: 단순 조회 요청임에도 @PostMapping과 @RequestBody 사용
    원인:
      - 페이지네이션 정보를 JSON Body로 전달하도록 설계
      - 무한 스크롤이나 단순 목록 조회에 비해 불필요하게 요청 복잡도 증가
    개선안:
      - @GetMapping으로 변경하고, 페이지·카테고리 등은 @RequestParam으로 전달
        (ex - /products?category=book&page=1&size=10)
      - 무한 스크롤 방식이라면 page 대신 cursor 기반 요청
    효과:
      - RESTful 원칙 준수 (조회 요청 → GET)
      - API 사용성 향상
      - 무한 스크롤 구현 시 FE/BE 간 데이터 흐름 단순화
    */
    @PostMapping(value = "/product/list")
    public ResponseEntity<ProductListResponse> getProductListByCategory(@RequestBody GetProductListRequest dto){
        Page<Product> productList = productService.getListByCategory(dto);
        return ResponseEntity.ok(new ProductListResponse(productList.getContent(), productList.getTotalPages(), productList.getTotalElements(), productList.getNumber()));
    }

    /*
    문제: 동일한 카테고리 목록에 대한 반복 조회 발생 가능성
    원인:
      - 카테고리 데이터는 변경 빈도가 낮지만 모든 요청마다 DB 조회 수행
      - 불필요한 I/O 부하 발생 및 응답 지연 가능, 불필요하게 DB Connection을 점유
    개선안:
      - 캐싱 도입 (카테고리 변경 시 무효화 전략 설계)
      - 변경 주기가 명확하지 않다면 TTL 기반 캐싱 고려
    효과:
      - DB 부하 감소
      - 응답 속도 향상
      - API 안정성 개선
    */
    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
