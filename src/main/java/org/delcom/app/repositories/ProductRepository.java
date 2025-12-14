package org.delcom.app.repositories;

import org.delcom.app.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    // Custom query untuk Fitur 8: Tampilkan Chart Data (Agregasi)
    // Mengambil Kategori dan hitungan jumlah produk per kategori
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countProductsByCategory();
    
    // Menghitung total stok
    @Query("SELECT SUM(p.stockQuantity) FROM Product p")
    Integer sumStockQuantity();
}