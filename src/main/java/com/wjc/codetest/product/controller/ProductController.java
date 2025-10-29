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

    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
