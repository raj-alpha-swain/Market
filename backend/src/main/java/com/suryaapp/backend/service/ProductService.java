package com.suryaapp.backend.service;

import com.suryaapp.backend.dto.product.ProductRequest;
import com.suryaapp.backend.dto.product.ProductResponse;
import com.suryaapp.backend.entity.Category;
import com.suryaapp.backend.entity.Product;
import com.suryaapp.backend.entity.ProductImage;
import com.suryaapp.backend.entity.User;
import com.suryaapp.backend.repository.CategoryRepository;
import com.suryaapp.backend.repository.ProductRepository;
import com.suryaapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        return productRepository.searchProducts(query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByCategory(Long categoryId, String query) {
        return productRepository.searchProductsByCategory(categoryId, query).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String username) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(Double.parseDouble(request.getPrice().toString()));
        product.setCategory(category);
        product.setStock(request.getStock());
        product.setVideoUrl(request.getVideoUrl());
        product.setActive(request.getActive() != null ? request.getActive() : true);
        product.setCreatedBy(creator);

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (!product.getCategory().getId().equals(request.getCategoryId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(newCategory);
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(Double.parseDouble(request.getPrice().toString()));
        product.setStock(request.getStock());
        product.setVideoUrl(request.getVideoUrl());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Transactional
    public ProductResponse addProductImage(Long productId, String imageUrl, Boolean isPrimary, Integer displayOrder) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // If this is set as primary, unset other primary images
        if (isPrimary != null && isPrimary) {
            product.getImages().forEach(img -> img.setIsPrimary(false));
        }

        ProductImage image = new ProductImage();
        image.setImageUrl(imageUrl);
        image.setIsPrimary(isPrimary != null ? isPrimary : false);
        image.setDisplayOrder(displayOrder != null ? displayOrder : product.getImages().size());
        image.setProduct(product);

        product.getImages().add(image);
        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(Double.parseDouble(product.getPrice().toString()));
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setStock(product.getStock());
        response.setVideoUrl(product.getVideoUrl());
        response.setActive(product.getActive());
        response.setCreatedBy(product.getCreatedBy() != null ? product.getCreatedBy().getUsername() : null);
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map images
        if (product.getImages() != null) {
            List<ProductResponse.ProductImageInfo> imageInfos = product.getImages().stream()
                    .map(img -> new ProductResponse.ProductImageInfo(
                            img.getId(),
                            img.getImageUrl(),
                            img.getIsPrimary(),
                            img.getDisplayOrder()))
                    .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                    .collect(Collectors.toList());
            response.setImages(imageInfos);
        } else {
            response.setImages(new ArrayList<>());
        }

        return response;
    }
}
