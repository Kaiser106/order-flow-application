package com.orderflow.product.entity;

import com.orderflow.common.entity.BaseEntity;
import com.orderflow.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor

public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;


    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;


}
