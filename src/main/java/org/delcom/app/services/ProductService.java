package org.delcom.app.services;

import org.delcom.app.dto.ProductRequest;
import org.delcom.app.entities.Product;
import org.delcom.app.repositories.ProductRepository;
import org.delcom.app.utils.FileStorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageUtil fileStorageUtil; 

    public ProductService(ProductRepository productRepository, FileStorageUtil fileStorageUtil) {
        this.productRepository = productRepository;
        this.fileStorageUtil = fileStorageUtil;
    }

    // Fitur 2: Tambah Data (Create)
    public Product createProduct(ProductRequest request) {
        Product product = new Product(/* mapping dari request */);
        // Asumsi: userId diambil dari Auth, tapi di sini kita pakai data dari request/default
        // product.setUserId(request.getUserId()); 
        // Lakukan mapping atribut dari request ke entity Product
        // ... (Harus dilakukan secara lengkap) ...
        return productRepository.save(product);
    }

    // Fitur 6 & 7: Tampilkan Daftar Data & Tampilkan Detail Data (Read)
    public List<Product> findAllProducts() {
        return productRepository.findAll(); // Fitur 6
    }

    public Optional<Product> findProductById(UUID id) {
        return productRepository.findById(id); // Fitur 7
    }

    // Fitur 3: Ubah Data (Update)
    public Product updateProduct(UUID id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));
        
        // Lakukan update atribut 
        // existingProduct.setName(request.getName());
        // existingProduct.setPrice(request.getPrice());
        // ... (Harus dilakukan secara lengkap) ...
        
        return productRepository.save(existingProduct);
    }
    
    // Fitur 4: Ubah Data Gambar (Update Gambar)
    public Product updateProductImage(UUID id, MultipartFile file) throws IOException {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product tidak ditemukan"));
        
        // 1. Upload file
        String newImageUrl = fileStorageUtil.uploadFile(file); 
        
        // 2. Update imageUrl di entity
        product.setImageUrl(newImageUrl);

        return productRepository.save(product);
    }

    // Fitur 5: Hapus Data (Delete)
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }

    // Fitur 8: Tampilkan Chart Data (Agregasi)
    public Map<String, Object> getProductChartData() {
        
        // Agregasi 1: Hitungan Produk per Kategori
        List<Object[]> categoryCounts = productRepository.countProductsByCategory();
        Map<String, Long> categoryChart = new HashMap<>();
        for (Object[] result : categoryCounts) {
            categoryChart.put((String) result[0], (Long) result[1]);
        }
        
        // Agregasi 2: Total Stok
        Integer totalStock = productRepository.sumStockQuantity();
        if (totalStock == null) totalStock = 0;

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("productsByCategory", categoryChart);
        chartData.put("totalStock", totalStock);
        chartData.put("totalProducts", productRepository.count());
        
        return chartData;
    }
}