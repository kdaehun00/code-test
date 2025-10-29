package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
/*
문제: 객체 불변성이 훼손됨, 사이드 이펙트 추적의 어려움.
원인: Setter를 Class 단에서 사용하면 모든 필드에 public setter 생성 -> 내부 데이터가 의도치 않게 변할 우려가 있음.
개선안: 1. 일부 데이터만 setter를 사용한다. 2. 유연성이 필요한 경우 메서드로 분리한다.
*/
@Setter
public class Product {

    @Id
    /*
    문제: null값이 들어올 수 있는 상황
    원인: @Column(nullable = true)가 기본값이기 때문에,
      DB 컬럼에 null이 허용됨 → 데이터 무결성 저해
    개선안: 1. @Column(nullable = false)로 명시하여 not null 제약 조건을 추가
          2. 엔티티 생성 시 생성자/Builder 패턴을 통해 필수 값 검증 로직 추가
    */
    @Column(name = "product_id")
    /*
    문제: DB별로 기본 키 생성 전략이 다르기 때문에,
        AUTO 전략은 예측 불가능한 동작을 야기할 수 있음.
    원인: GenerationType.AUTO는 JPA 구현체가 DB에 따라 자동으로 ID 생성 전략을 선택하기 때문
    개선안: 프로젝트에서 사용하는 DB에 맞춰 명시적으로 전략 설정
      - MySQL: GenerationType.IDENTITY
      - Oracle / PostgreSQL: GenerationType.SEQUENCE
      - DB 독립적으로 관리하고 싶다면 @TableGenerator 사용 고려
    트레이드오프:
    - AUTO는 DB를 바꿔도 별도 수정 없이 동작 가능 -> 유연성 높음
    - 그러나 실제 운영 DB가 바뀌면 ID 생성 방식이 달라져 예기치 않은 문제 발생 가능
    */
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /*
    문제: category를 String으로 관리하면 오타, 잘못된 값, 대소문자 불일치 등 데이터 무결성 저하 가능성이 있음 (자주 바뀌지 않는 값이라는 가정 하에)
    원인: category가 단순 String 타입이기 때문에 @Valid와 같은 Bean Validation을 통한 도메인 제약 검증이 어려움.
    개선안: Enum 타입으로 category를 관리하여 코드 레벨에서 유효값을 한정
        - 매핑 시:
          @Enumerated(EnumType.STRING) -> 없으면 enum의 순서 숫자를 DB에 저장할 수도 있음.
          private Category category;
    선택 근거:
    - category의 값은 제한된 집합이고, 오타나 유효하지 않은 문자열을 막는 것이 중요하므로 Enum 사용이 적절함
    - DB 차원에서는 유효성 검증을 하기보다는 확장성을 위해 애플리케이션 레벨에서만 유효성 검증을 해야 함
    */
    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    /*
    문제: 생성자 내 필드가 어떤 값인지 예측하기 어려움. 유연성이 낮음.
    원인: 불필요하게 직접 필드를 세팅하는 생성자를 작성하여 객체 생성 방식이 불명확해짐
    개선안:
      - 불변성과 일관성 확보를 위해 @RequiredArgsConstructor와 @Builder로 대체
      - 객체 생성 시 필요한 필드에 final 추가
    효과:
      - 필요한 데이터만 설정 가능
      - 유연성 확보
      - 가독성 향상 (builder 사용 시 .name("value") 과 같은 형태로 명시적임.)
      - 변경 가능성 최소화
    */
    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }

    /*
    문제: 불필요한 코드로 가독성 저하
    원인: getter와 중복 사용
    개선안: getter 사용으로 깔끔한 코드 구조 유지
    */

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
