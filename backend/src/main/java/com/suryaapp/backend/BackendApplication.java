package com.suryaapp.backend;

import com.suryaapp.backend.entity.Category;
import com.suryaapp.backend.entity.Product;
import com.suryaapp.backend.entity.User;
import com.suryaapp.backend.repository.CategoryRepository;
import com.suryaapp.backend.repository.ProductRepository;
import com.suryaapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create hardcoded admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@suryaapp.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin user created: username=admin, password=admin123");
            }

            // Create default categories
            createCategoryIfNotExists(categoryRepository, "Masala", "Aromatic spices and masala blends");
            createCategoryIfNotExists(categoryRepository, "Dry Fruits", "Premium dry fruits and nuts");
            createCategoryIfNotExists(categoryRepository, "Spices", "Fresh and pure spices from farms");
            createCategoryIfNotExists(categoryRepository, "Nuts", "Roasted and raw nuts");
            createCategoryIfNotExists(categoryRepository, "Seeds", "Healthy seeds for cooking");

            // Create sample products
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null && productRepository.count() == 0) {
                createSampleProducts(categoryRepository, productRepository, admin);
            }
        };
    }

    private void createCategoryIfNotExists(CategoryRepository repo, String name, String desc) {
        if (!repo.existsByName(name)) {
            Category category = new Category();
            category.setName(name);
            category.setDescription(desc);
            category.setActive(true);
            repo.save(category);
            System.out.println("Category created: " + name);
        }
    }

    private void createSampleProducts(CategoryRepository categoryRepo,
            ProductRepository productRepo, User admin) {
        System.out.println("Creating sample products...");

        // Masala products
        Category masala = categoryRepo.findByName("Masala").orElse(null);
        if (masala != null) {
            createProduct(productRepo, "Garam Masala", "Premium blend of aromatic spices for authentic Indian cooking",
                    120.0, masala, 50, admin);
            createProduct(productRepo, "Turmeric Powder", "Pure and organic turmeric powder with high curcumin content",
                    80.0, masala, 100, admin);
            createProduct(productRepo, "Chaat Masala", "Tangy and spicy chaat masala for snacks and salads",
                    90.0, masala, 75, admin);
        }

        // Dry Fruits products
        Category dryFruits = categoryRepo.findByName("Dry Fruits").orElse(null);
        if (dryFruits != null) {
            createProduct(productRepo, "California Almonds", "Premium quality almonds rich in protein and vitamins",
                    599.0, dryFruits, 30, admin);
            createProduct(productRepo, "Cashew Nuts", "Whole cashews, perfectly roasted and lightly salted",
                    799.0, dryFruits, 25, admin);
            createProduct(productRepo, "Dried Apricots", "Sweet and nutritious dried apricots packed with fiber",
                    450.0, dryFruits, 40, admin);
            createProduct(productRepo, "Black Raisins", "Natural black raisins without added sugar",
                    199.0, dryFruits, 60, admin);
        }

        // Spices products
        Category spices = categoryRepo.findByName("Spices").orElse(null);
        if (spices != null) {
            createProduct(productRepo, "Red Chilli Powder", "Hot and vibrant red chilli powder from Guntur",
                    70.0, spices, 80, admin);
            createProduct(productRepo, "Coriander Powder", "Fresh ground coriander powder with natural aroma",
                    60.0, spices, 90, admin);
            createProduct(productRepo, "Cumin Seeds", "Aromatic cumin seeds for tempering and cooking",
                    150.0, spices, 70, admin);
        }

        // Nuts products
        Category nuts = categoryRepo.findByName("Nuts").orElse(null);
        if (nuts != null) {
            createProduct(productRepo, "Walnut Kernels", "Premium walnut halves rich in omega-3",
                    899.0, nuts, 20, admin);
            createProduct(productRepo, "Roasted Pistachios", "Unsalted roasted pistachios from California",
                    1299.0, nuts, 15, admin);
        }

        // Seeds products
        Category seeds = categoryRepo.findByName("Seeds").orElse(null);
        if (seeds != null) {
            createProduct(productRepo, "Chia Seeds", "Organic chia seeds packed with nutrients",
                    299.0, seeds, 45, admin);
            createProduct(productRepo, "Pumpkin Seeds", "Roasted & salted pumpkin seeds for healthy snacking",
                    349.0, seeds, 35, admin);
            createProduct(productRepo, "Flax Seeds", "Golden flax seeds rich in fiber and omega-3",
                    179.0, seeds, 50, admin);
        }

        System.out.println("✅ Sample products created successfully!");
    }

    private void createProduct(ProductRepository repo, String name, String description,
            Double price, Category category, Integer stock, User createdBy) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(Double.parseDouble(price.toString()));
        product.setCategory(category);
        product.setStock(stock);
        product.setActive(true);
        product.setCreatedBy(createdBy);
        repo.save(product);
    }
}
