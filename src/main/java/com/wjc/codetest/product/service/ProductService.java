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
/*
문제: 현재 ProductService에 @Slf4j만 선언되어 있고, 실제 로그 호출이 없음
원인: DB I/O 또는 상태 변경이 발생하는 메서드에서 로그를 남기지 않아 운영 중 데이터 흐름 추적이 어려움
개선안:
  - create, update, delete 등 상태 변경 메서드에서 info/debug 수준의 로그 추가
  - 필요 시 예외 발생 시 warn/error 로그 기록
효과:
  - 운영 중 원인 추적 용이
  - 서비스 내부 데이터 흐름 가시성 향상
  - 문제 발생 시 빠른 대응 가능
*/
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

    /*
    문제:
      - 삭제 로직이 서비스 계층에 직접 구현되어 있어 도메인 규칙과의 분리가 모호함
      - 도메인 객체의 불변성 및 원자성을 보장하기 어려움
    원인:
      - 도메인 내부에 삭제 책임을 위임하지 않고 서비스 레벨에서 직접 Repository를 호출함
      - 도메인 주도 설계 관점의 애그리게잇 경계를 고려하지 않음
    개선안:
      - 도메인 엔티티 내에 `delete()` 메서드를 정의하여 스스로 상태를 관리하도록 변경
        → 상태 플래그 변경(soft delete) 포함
      - 서비스 계층은 도메인 로직을 단순히 호출만 하도록 역할 단순화
    효과:
      - 도메인 중심의 책임 분리 및 비즈니스 규칙 일관성 확보
      - 삭제 로직 변경 시 서비스 영향 최소화
      - 유지보수성과 테스트 용이성 향상
    */
    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    /*
    문제:
      - 만약 category를 따로 테이블을 분리하고 연관관계를 설정했을 경우 -
      - @EntityGraph 또는 fetch join과 Pageable을 함께 사용 시 JPA에서 경고 발생
      - 메모리 내 페이징으로 인한 성능 저하 및 대용량 데이터 처리 불가
    원인:
      - 일대다 관계에서 JOIN 사용 시 자식 엔티티 개수만큼 부모 엔티티가 중복 조회됨
      - JPA는 이를 위험하다 판단하여 DB 쿼리 결과를 메모리로 가져온 후 페이징 처리
      - 결과적으로 LIMIT/OFFSET이 DB 레벨에서 적용되지 않고 풀 테이블 스캔 발생
    개선안:
      1. id를 먼저 페이지네이션으로 조회
      2. 그 id 목록을 바탕으로 fetch join으로 관련 데이터 조회
    효과:
      - DB 레벨에서 LIMIT/OFFSET 적용으로 성능 개선
      - 메모리 페이징 제거로 대용량 데이터 처리 가능
      - 정확한 페이지네이션 동작
    */
    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }

    /*
    문제: Product 수가 많아질수록 getUniqueCategories 쿼리 속도가 느려질 수 있음
    원인: Product 테이블에서 category를 추출하기 때문에, 대량 데이터일 경우 full scan 발생 가능
    개선안:
      - category를 별도 테이블로 분리하고 Product와 연관관계를 맺어 관리
      - 필요 시 캐싱 적용으로 조회 부담 최소화
    효과:
      - 쿼리 성능 개선
      - 도메인 책임 분리 및 유지보수 용이성 향상
      - 빈번한 category 조회 시 서비스 안정성 증가, 활용도 증가
    */
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
