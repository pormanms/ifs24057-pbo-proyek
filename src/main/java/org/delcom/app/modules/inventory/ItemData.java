package org.delcom.app.modules.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ItemData {
    @NotBlank(message = "Nama produk wajib diisi")
    private String name;

    @NotBlank(message = "Kategori wajib diisi")
    private String category;

    @NotNull(message = "Harga wajib diisi")
    @Min(value = 0)
    private Double price;

    @NotNull(message = "Stok wajib diisi")
    @Min(value = 0)
    private Integer stock;

    private String description;
    private MultipartFile imageFile;

    // Getters Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MultipartFile getImageFile() { return imageFile; }
    public void setImageFile(MultipartFile imageFile) { this.imageFile = imageFile; }
}