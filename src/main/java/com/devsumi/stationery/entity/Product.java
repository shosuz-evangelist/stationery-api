package com.devsumi.stationery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
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

    @ElementCollection
    @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "tag")
    private List<String> tags;

    private String brand;
    private Integer stock;
    private String imageUrl;

    @Column(columnDefinition = "vector(1536)")
    @Type(org.hibernate.type.SerializableType.class)
    private float[] embedding;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
