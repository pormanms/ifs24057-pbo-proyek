package org.delcom.app.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.delcom.app.dto.ProductRequest;
import org.delcom.app.entities.Product;
import org.delcom.app.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product testProduct;
    private UUID testProductId = UUID.randomUUID();
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(testProductId);
        testProduct.setName("Laptop Gaming");
        testProduct.setPrice(new BigDecimal("15000000"));
        testProduct.setCategory("Elektronik");

        testProductRequest = new ProductRequest();
        testProductRequest.setName("Laptop Gaming");
        testProductRequest.setPrice(new BigDecimal("15000000"));
        testProductRequest.setCategory("Elektronik");
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProductId.toString()))
                .andExpect(jsonPath("$.name").value("Laptop Gaming"));

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        List<Product> products = Collections.singletonList(testProduct);
        when(productService.findAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop Gaming"));
        
        verify(productService, times(1)).findAllProducts();
    }

    @Test
    void getProductById_found_shouldReturnProduct() throws Exception {
        when(productService.findProductById(testProductId)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/{id}", testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Gaming"));
    }

    @Test
    void getProductById_notFound_shouldReturnNotFound() throws Exception {
        when(productService.findProductById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Laptop");
        when(productService.updateProduct(eq(testProductId), any(ProductRequest.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/{id}", testProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(testProductId);

        mockMvc.perform(delete("/api/products/{id}", testProductId))
                .andExpect(status().isNoContent());
        
        verify(productService, times(1)).deleteProduct(testProductId);
    }
    
    @Test
    void updateProductImage_shouldReturnUpdatedProduct() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "image data".getBytes()
        );

        Product productWithImage = new Product();
        productWithImage.setImageUrl("/uploads/test-image.jpg");
        when(productService.updateProductImage(eq(testProductId), any())).thenReturn(productWithImage);

        mockMvc.perform(multipart("/api/products/{id}/image", testProductId)
                .file(mockFile)
                .with(request -> { request.setMethod("PUT"); return request; })
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/uploads/test-image.jpg"));
    }

    @Test
    void getProductChartData_shouldReturnMapData() throws Exception {
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("totalProducts", 100L);
        when(productService.getProductChartData()).thenReturn(chartData);

        mockMvc.perform(get("/api/products/chart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(100));
    }
}