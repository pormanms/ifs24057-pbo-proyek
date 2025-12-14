package org.delcom.app.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    // ... (Atribut seperti sebelumnya) ...
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; 

    @Column(nullable = false, length = 150)
    private String name;

    @Lob 
    private String description;

    @Column(nullable = false)
    private BigDecimal price; 

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0; 
    
    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "image_url")
    private String imageUrl; 

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Konstruktor (Bisa ditambahkan jika diperlukan) ---
    public Product() {}

    // --- Getter dan Setter (Bagian yang HILANG) ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    // INI YANG MEMPERBAIKI ERROR UTAMA:
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } 
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- Otomatisasi Waktu (Seperti sebelumnya) ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}