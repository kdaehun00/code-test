package com.wjc.codetest.product.repository;

import com.wjc.codetest.product.model.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    /*
    문제: 메서드 파라미터 이름이 혼란을 유발할 수 있음
    원인:
      - findAllByCategory(String name, Pageable pageable)에서 category 대신 name이라는 파라미터명을 사용
      - 메서드명과 파라미터명이 불일치하여 읽는 사람에게 혼동 가능
    개선안:
      - 파라미터명을 category로 변경하여 메서드명과 일치시키기
        (ex - findAllByCategory(String category, Pageable pageable))
    효과:
      - 가독성 향상
      - 메서드 의도와 파라미터 의미 명확화
      - 코드 유지보수성 향상
    */
    /*
    문제: 삭제된 데이터까지 포함될 가능성
    원인:
      - findAllByCategory(String, Pageable) 메서드에서 deleted 여부 필터링 미적용
      - soft delete 적용 시, 실제 사용자가 삭제된 상품을 조회할 수 있음
    개선안:
      - JPQL로 선언 후 where deleted_at IS NULL 조건 추가 가능
    효과:
      - 삭제된 데이터가 조회되지 않아 사용자 혼란 방지
      - soft delete 정책과 조회 로직 일관성 확보
    */
    Page<Product> findAllByCategory(String name, Pageable pageable);

    /*
    문제: DISTINCT로 인한 풀 테이블 스캔 발생
    원인:
      - DISTINCT는 모든 행을 읽어 중복 제거 필요
      - 인덱스 활용 어려움
    개선안:
      - GROUP BY를 사용하여 인덱스 활용
      - category 테이블을 따로 분리
    효과:
      - 인덱스 활용으로 성능 개선
      - 풀 테이블 스캔 방지
    좋아보이는 방식:
      - category 테이블 분리
    이유:
      - 현재는 category가 변하면 관련된 모든 Product의 category 컬럼을 변경해야함.
      - 테이블을 분리하면 특정 값만 변경하면 됨.
    */
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}
