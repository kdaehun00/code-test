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
