package com.suryaapp.backend.controller;

import com.suryaapp.backend.dto.product.ProductRequest;
import com.suryaapp.backend.dto.product.ProductResponse;
import com.suryaapp.backend.service.ProductService;
import com.suryaapp.backend.service.FileStorageService;
import com.suryaapp.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    @GetMapping("/category/{categoryId}/search")
    public ResponseEntity<List<ProductResponse>> searchProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam String q) {
        return ResponseEntity.ok(productService.searchProductsByCategory(categoryId, q));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsername(authHeader);
            ProductResponse response = productService.createProduct(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse response = productService.updateProduct(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary) {
        try {
            List<String> imageUrls = fileStorageService.saveProductImages(files);

            ProductResponse response = null;
            for (int i = 0; i < imageUrls.size(); i++) {
                boolean primary = (isPrimary != null && isPrimary && i == 0);
                response = productService.addProductImage(id, imageUrls.get(i), primary, i);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<?> uploadProductVideo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String videoUrl = fileStorageService.saveProductVideo(file);

            // Update product with video URL
            ProductResponse product = productService.getProductById(id);
            ProductRequest updateRequest = new ProductRequest();
            updateRequest.setName(product.getName());
            updateRequest.setDescription(product.getDescription());
            updateRequest.setPrice(product.getPrice());
            updateRequest.setCategoryId(product.getCategoryId());
            updateRequest.setStock(product.getStock());
            updateRequest.setVideoUrl(videoUrl);
            updateRequest.setActive(product.getActive());

            ProductResponse response = productService.updateProduct(id, updateRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private String extractUsername(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUsername(token);
        }
        return "admin"; // Default for testing
    }

    record ErrorResponse(String error) {
    }
}
