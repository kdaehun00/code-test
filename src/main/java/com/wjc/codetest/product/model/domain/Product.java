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
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "category")
    private String category;

    @Column(name = "name")
    private String name;

    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}
