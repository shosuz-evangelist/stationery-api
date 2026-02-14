package com.devsumi.stationery.controller;

import com.devsumi.stationery.entity.Product;
import com.devsumi.stationery.repository.ProductRepository;
import com.devsumi.stationery.service.AIRoutingEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final AIRoutingEngine aiRoutingEngine;

    @GetMapping
    public List<Product> getProducts(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(defaultValue = "20") int limit,
        @RequestParam(defaultValue = "0") int offset
    ) {
        // シンプルな例: 全件取得
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        return productRepository.findById(id).map(existing -> {
            product.setId(id);
            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> semanticSearch(@RequestBody Map<String, Object> req) {
        String query = (String) req.get("query");
        int limit = (int) req.getOrDefault("limit", 10);
        double threshold = (double) req.getOrDefault("threshold", 0.7);

        float[] queryEmbedding = aiRoutingEngine.createEmbedding(query);
        String vectorStr = Arrays.toString(queryEmbedding)
            .replace("[", "")
            .replace("]", "");

        List<Object[]> results = productRepository.semanticSearch(vectorStr, threshold, limit);
        List<Map<String, Object>> products = new ArrayList<>();
        for (Object[] row : results) {
            Product p = (Product) row[0];
            Double similarity = (Double) row[row.length - 1];
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("price", p.getPrice());
            map.put("category", p.getCategory());
            map.put("tags", p.getTags());
            map.put("brand", p.getBrand());
            map.put("stock", p.getStock());
            map.put("image_url", p.getImageUrl());
            map.put("similarity", similarity);
            products.add(map);
        }
        return ResponseEntity.ok(Map.of("results", products));
    }

    @PostMapping("/generate-embeddings")
    public ResponseEntity<Map<String, Object>> generateEmbeddings() {
        List<Product> targets = productRepository.findByEmbeddingIsNull();
        int success = 0, failed = 0;
        for (Product p : targets) {
            try {
                String text = p.getName() + " " + p.getDescription();
                float[] emb = aiRoutingEngine.createEmbedding(text);
                p.setEmbedding(emb);
                productRepository.save(p);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        return ResponseEntity.ok(Map.of(
            "processed", targets.size(),
            "success", success,
            "failed", failed
        ));
    }
}
