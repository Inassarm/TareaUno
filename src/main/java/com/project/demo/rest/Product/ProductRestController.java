package com.project.demo.rest.Product;

import com.project.demo.logic.entity.Category.Category;
import com.project.demo.logic.entity.Category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'USER')")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @RequestBody Product product, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);

        if (foundProduct.isPresent()) {
            Product existingProduct = foundProduct.get();
            Long categoryId = product.getCategory().getId();
            Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isEmpty()) {
                return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found",
                        HttpStatus.BAD_REQUEST, request);
            }
            product.setId(existingProduct.getId());
            product.setCategory(categoryOptional.get());
            productRepository.save(product);

            return new GlobalResponseHandler().handleResponse("Product updated successfully",
                    product, HttpStatus.OK, request);

        } else {
            return new GlobalResponseHandler().handleResponse("Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> createProduct(@RequestBody Product product, HttpServletRequest request) {
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            return new GlobalResponseHandler().handleResponse("Category ID is required",
                    HttpStatus.BAD_REQUEST, request);
        }

        Optional<Category> foundCategory = categoryRepository.findById(product.getCategory().getId());
        if (foundCategory.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("Category id " + product.getCategory().getId() + " not found",
                    HttpStatus.NOT_FOUND, request);
        }

        product.setCategory(foundCategory.get());
        Product savedProduct = productRepository.save(product);

        return new GlobalResponseHandler().handleResponse("Product created successfully",
                savedProduct, HttpStatus.CREATED, request);
    }


    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);

        if (foundProduct.isPresent()) {
            productRepository.deleteById(productId);
            return new GlobalResponseHandler().handleResponse("Product deleted successfully",
                    foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

}