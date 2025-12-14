package org.delcom.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductRequest {
    private UUID userId; 
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
    
    // --- Getter dan Setter (WAJIB DITAMBAH UNTUK MEMPERBAIKI ERROR) ---
    
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { // MEMPERBAIKI ERROR setName(String)
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) { // MEMPERBAIKI ERROR setPrice(BigDecimal)
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) { // MEMPERBAIKI ERROR setStockQuantity(int)
        this.stockQuantity = stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) { // MEMPERBAIKI ERROR setCategory(String)
        this.category = category;
    }
}