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
    @PostMapping(value = "/delete/product/{productId}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

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
