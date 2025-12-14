package org.delcom.app.services;

import org.delcom.app.dto.ProductRequest;
import org.delcom.app.entities.Product;
import org.delcom.app.repositories.ProductRepository;
import org.delcom.app.utils.FileStorageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Menggunakan MockitoExtension untuk mengaktifkan anotasi Mockito
@ExtendWith(MockitoExtension.class) 
class ProductServiceTest {

    // Inject the mocked dependencies into the service
    @InjectMocks
    private ProductService productService;

    // Mocking dependencies
    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageUtil fileStorageUtil;
    
    @Mock
    private MultipartFile mockFile;

    private Product testProduct;
    private UUID testProductId = UUID.randomUUID();
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(testProductId);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("100"));
        testProduct.setStockQuantity(10);
        testProduct.setCategory("Gadget");

        testProductRequest = new ProductRequest();
        testProductRequest.setName("New Name");
        testProductRequest.setPrice(new BigDecimal("200"));
        testProductRequest.setStockQuantity(20);
        testProductRequest.setCategory("Aksesoris");
    }

    // Test untuk Fitur 2: Create Product
    @Test
    void createProduct_shouldSaveAndReturnProduct() {
        // Mocking ProductRepository.save() untuk mengembalikan produk yang disimpan
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProductRequest);

        assertNotNull(result);
        assertEquals("Test Product", result.getName()); 
        // Verifikasi bahwa save() dipanggil satu kali
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // Test untuk Fitur 7: Find Product By Id - Found
    @Test
    void findProductById_shouldReturnProduct() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.findProductById(testProductId);

        assertTrue(result.isPresent());
        assertEquals(testProductId, result.get().getId());
    }

    // Test untuk Fitur 3: Update Product - Successful
    @Test
    void updateProduct_shouldUpdateAndSaveProduct() {
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(testProductId, testProductRequest);

        // Catatan: Dalam kode service, Anda perlu melakukan mapping update 
        // secara eksplisit agar test ini benar-benar memverifikasi update.
        // Di sini kita hanya memverifikasi pemanggilan.
        assertNotNull(result);
        verify(productRepository, times(1)).findById(testProductId);
        verify(productRepository, times(1)).save(any(Product.class));
    }
    
    // Test untuk Fitur 4: Update Product Image
    @Test
    void updateProductImage_shouldUploadFileAndSaveUrl() throws IOException {
        String newUrl = "/uploads/new_image.png";
        
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(fileStorageUtil.uploadFile(any(MultipartFile.class))).thenReturn(newUrl);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        Product result = productService.updateProductImage(testProductId, mockFile);
        
        assertNotNull(result);
        assertEquals(newUrl, result.getImageUrl());
        verify(fileStorageUtil, times(1)).uploadFile(mockFile);
        verify(productRepository, times(1)).save(testProduct);
    }
    
    // Test untuk Fitur 5: Delete Product
    @Test
    void deleteProduct_shouldCallDeleteRepository() {
        // Repository tidak perlu mock 'when' karena deleteById adalah void
        productService.deleteProduct(testProductId);
        
        verify(productRepository, times(1)).deleteById(testProductId);
    }
    
    // Test untuk Fitur 8: Get Chart Data
    @Test
    void getProductChartData_shouldReturnAggregatedData() {
        // Mocking Agregasi 1
        List<Object[]> categoryCounts = Arrays.asList(
                new Object[]{"Elektronik", 5L},
                new Object[]{"Pakaian", 10L}
        );
        when(productRepository.countProductsByCategory()).thenReturn(categoryCounts);

        // Mocking Agregasi 2
        when(productRepository.sumStockQuantity()).thenReturn(150);
        
        // Mocking Total Products
        when(productRepository.count()).thenReturn(15L);

        Map<String, Object> chartData = productService.getProductChartData();

        assertTrue(chartData.containsKey("productsByCategory"));
        assertTrue(chartData.containsKey("totalStock"));
        assertEquals(150, chartData.get("totalStock"));
        assertEquals(15L, chartData.get("totalProducts"));
    }
}