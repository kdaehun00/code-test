package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    /*
    문제: Optional 처리 시 가독성 저하 및 불필요한 조건문 존재
    원인: isPresent() / get() 조합으로 null 체크 수행
    개선안: Optional의 orElseThrow() 사용으로 간결화
          ex - Product product = productRepository.findById(productId)
                 .orElseThrow(() -> new RuntimeException("product not found"));
    효과: 가독성 향상, NPE 방지, 코드 라인 수 감소
    */
    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {

            /*
            문제:
              - 예외 처리 일관성 부족
              - 하드코딩으로 인해 생산성 및 일관성 저하
              - 단순 런타임 에러로 예외 원인 구분 어려움
            원인: 에러를 단순 RuntimeException로 하드코딩
            개선안:
              - 커스텀 예외 클래스 생성 후 사용
                1. 커스텀 예외 클래스 생성 및 RuntimeException을 extends (ex - ProductException)
                2. 에러코드를 Global 파일에 enum으로 선언 및 도메인별 enum을 분리하고 상위 enum 상속
                3. GlobalExceptionHandler에 CustomException 추가
              - 전역 예외 처리 핸들러(@RestControllerAdvice)에서 공통 응답 포맷 관리
            효과:
              - 에러 로깅 및 사용자 응답 일관성 확보
              - 서비스 전반의 유지보수성 향상
            */
            throw new RuntimeException("product not found");
        }
        return productOptional.get();
    }

    /*
    문제: 메서드 간 반환 일관성 부족
    원인: create()에서는 save() 결과를 직접 반환하지만,
         update()에서는 별도 변수(updatedProduct)에 담아 반환하고 있음
    개선안: 두 메서드 모두 동일한 반환 패턴으로 통일 (return productRepository.save(...))
    효과: 코드 일관성 및 가독성 향상
    */
    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;

    }

    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}

/*
문제: 트랜잭션 경계 설정 미비
원인: @Transactional 미적용 → update/delete 수행 시 원자성 보장되지 않음
개선안:
  - 데이터 변경 메서드(create, update, delete)에 @Transactional 추가
  - 조회용 메서드에는 @Transactional(readOnly = true) 적용
효과:
  - 데이터 정합성 및 트랜잭션 안정성 확보
*/
