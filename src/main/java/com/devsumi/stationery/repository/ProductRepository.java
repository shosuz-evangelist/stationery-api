package com.devsumi.stationery.repository;

import com.devsumi.stationery.entity.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
        SELECT *, 1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
        FROM products
        WHERE embedding IS NOT NULL
        AND 1 - (embedding <=> CAST(:queryVector AS vector)) > :threshold
        ORDER BY embedding <=> CAST(:queryVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> semanticSearch(
        @Param("queryVector") String queryVector,
        @Param("threshold") double threshold,
        @Param("limit") int limit
    );

    List<Product> findByEmbeddingIsNull();
}
