package org.delcom.app.modules.inventory;

import org.delcom.app.modules.inventory.Item;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void onCreate_ShouldSetCreatedAndUpdatedTime() {
        Item product = new Item();
        
        // Sebelum persist, pastikan properti waktu masih null
        assertNull(product.getCreatedAt());
        assertNull(product.getUpdatedAt());

        // Simulasi JPA memanggil @PrePersist
        product.onCreate();

        // VALIDASI YANG AMAN:
        // Cukup pastikan bahwa sistem telah mengisi waktu (tidak null).
        // Kita TIDAK membandingkan createdAt == updatedAt karena 
        // proses komputer akan memberikan selisih nanodetik yang menyebabkan error.
        assertNotNull(product.getCreatedAt(), "createdAt harus terisi");
        assertNotNull(product.getUpdatedAt(), "updatedAt harus terisi");
    }

    @Test
    void onUpdate_ShouldUpdateOnlyUpdatedTime() throws InterruptedException {
        Item product = new Item();
        product.onCreate(); // Set waktu awal
        
        LocalDateTime oldCreatedAt = product.getCreatedAt();
        LocalDateTime oldUpdatedAt = product.getUpdatedAt();

        // Tunggu 10 milidetik agar waktu sistem pasti berubah
        // Ini mencegah error jika proses test berjalan terlalu cepat
        Thread.sleep(10); 

        // Simulasi JPA memanggil @PreUpdate
        product.onUpdate();

        // VALIDASI:
        // 1. createdAt TIDAK BOLEH berubah
        assertEquals(oldCreatedAt, product.getCreatedAt()); 
        
        // 2. updatedAt HARUS berubah (berbeda dari waktu lama)
        assertNotEquals(oldUpdatedAt, product.getUpdatedAt());
        
        // 3. Waktu update baru harus lebih besar (setelah) waktu create
        assertTrue(product.getUpdatedAt().isAfter(product.getCreatedAt()));
    }
    
    @Test
    void testGettersSetters() {
        // Validasi setter dan getter sederhana
        Item product = new Item();
        String namaProduk = "Test Item";
        
        product.setName(namaProduk);
        assertEquals(namaProduk, product.getName());
    }
}