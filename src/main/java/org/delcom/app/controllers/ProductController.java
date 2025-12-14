package org.delcom.app.controllers;

import org.delcom.app.dto.ProductRequest;
import org.delcom.app.entities.Product;
import org.delcom.app.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Fitur 2: Tambah Data (POST /api/products)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

    // Fitur 6: Tampilkan Daftar Data (GET /api/products)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAllProducts());
    }

    // Fitur 7: Tampilkan Detail Data (GET /api/products/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable UUID id) {
        return productService.findProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Fitur 3: Ubah Data (PUT /api/products/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable UUID id, @RequestBody ProductRequest request) {
        try {
            Product updatedProduct = productService.updateProduct(id, request);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Fitur 4: Ubah Data Gambar (PUT /api/products/{id}/image)
    @PutMapping("/{id}/image")
    public ResponseEntity<Product> updateProductImage(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        try {
            Product updatedProduct = productService.updateProductImage(id, file);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException | IOException e) {
            // Log e.getMessage() untuk debug yang sesungguhnya
            return ResponseEntity.badRequest().build(); 
        }
    }

    // Fitur 5: Hapus Data (DELETE /api/products/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    // Fitur 8: Tampilkan Chart Data (GET /api/products/chart)
    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> getProductChartData() {
        return ResponseEntity.ok(productService.getProductChartData());
    }
}