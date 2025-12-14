package org.delcom.app.controllers;

import org.delcom.app.entities.Product;
import org.delcom.app.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller // Menggunakan @Controller untuk mengembalikan nama view (HTML)
@RequestMapping("/")
public class WebController {

    private final ProductService productService;

    public WebController(ProductService productService) {
        this.productService = productService;
    }

    // Menggantikan tampilan default Todo List dengan tampilan Product List
    // Fitur 6: Tampilan Daftar Data
    @GetMapping
    public String showProductList(Model model) {
        List<Product> products = productService.findAllProducts();
        
        // Menambahkan data produk ke Model untuk diakses oleh template
        model.addAttribute("products", products);
        model.addAttribute("username", "Porman Marsaulina"); // Ganti dengan data dari Auth
        model.addAttribute("pageTitle", "Daftar Produk Marketplace Sederhana");
        
        // Mengembalikan nama template HTML yang baru
        return "product_list"; 
    }
    
    // ... Tambahkan endpoint untuk detail, tambah, ubah form di sini
    // Contoh untuk Detail (Fitur 7)
    // @GetMapping("/products/{id}")
    // public String showProductDetail(@PathVariable UUID id, Model model) { ... }
}