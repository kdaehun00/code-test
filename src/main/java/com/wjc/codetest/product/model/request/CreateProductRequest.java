package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
문제: 필드의 필수/선택 여부가 불명확하고 보일러플레이트 코드가 많음.

원인:
  1. 생성자 오버로딩으로 name의 필수 여부를 암시적으로만 표현
  2. @Getter/@Setter와 생성자를 직접 작성하여 코드량 증가
  3. setter로 인해 불변성 보장 안 됨

개선안:
  1. Java Record 사용
  2. name이 필수값이 아니라면 Optional<String>로 명시
    (category만 있는 것을 보아 name이 필수값이 아닌 것처럼 보임)

효과:
  - 코드 간결화
  - 필드 필수/선택 여부가 명확
*/
public class CreateProductRequest {
    /*
    문제: 유효하지 않은 데이터를 허용할 가능성이 있음
    원인: 빈칸이나 null 값에 대한 검증이 없음.
    개선안: @NotBlank, @Valid 등 검증 애노테이션을 사용하여 데이터 유효성 검사 수행
    */
    private String category;
    private String name;

    public CreateProductRequest(String category) {
        this.category = category;
    }

    public CreateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}

