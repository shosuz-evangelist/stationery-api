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
        String vectorStr = Arrays.toString(queryEmbedding);

        List<Object[]> results = productRepository.semanticSearch(vectorStr, threshold, limit);
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            map.put("description", row[2]);
            map.put("price", row[3]);
            map.put("category", row[4]);
            map.put("tags", row[5]);
            map.put("brand", row[6]);
            map.put("stock", row[7]);
            map.put("imageUrl", row[8]);
            map.put("similarity", row[row.length - 1]);
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

    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(@RequestBody Map<String, Object> req) {
        Integer userId = (Integer) req.get("userId");
        String context = (String) req.get("context");
        
        float[] contextEmbedding = aiRoutingEngine.createEmbedding(context);
        String vectorStr = Arrays.toString(contextEmbedding);
        
        List<Object[]> searchResults = productRepository.semanticSearch(vectorStr, 0.6, 20);
        
        List<Map<String, Object>> products = new ArrayList<>();
        for (Object[] row : searchResults) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", row[0]);
            product.put("name", row[1]);
            product.put("description", row[2]);
            product.put("price", row[3]);
            product.put("category", row[4]);
            products.add(product);
        }
        
        String prompt = String.format(
            "ユーザーID %d が「%s」というコンテキストで文房具を探しています。以下の商品から5つ選んで推薦理由とともに提案してください：\n\n%s",
            userId, context, products.toString()
        );
        
        String recommendation = aiRoutingEngine.generateText(prompt);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "context", context,
            "recommendation", recommendation,
            "candidateProducts", products
        ));
    }
}
