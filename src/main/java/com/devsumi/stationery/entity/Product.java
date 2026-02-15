package com.devsumi.stationery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Double price;
    private String category;

    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    private String brand;
    private Integer stock;
    private String imageUrl;

    @org.hibernate.annotations.Type(com.devsumi.stationery.hibernate.VectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
