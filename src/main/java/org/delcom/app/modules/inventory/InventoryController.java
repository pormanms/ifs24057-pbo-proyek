package org.delcom.app.modules.inventory;

import org.delcom.app.modules.authentication.User;
import org.delcom.app.modules.authentication.AccountService;
import org.delcom.app.services.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class InventoryController {

    private final ItemService productService;
    private final FileStorageService fileStorageService;
    private final AccountService userService;

    public InventoryController(ItemService productService, FileStorageService fileStorageService,
            AccountService userService) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    // --- HELPER: AMBIL USER DARI SESSION ---
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User principal = (User) auth.getPrincipal();
            if (principal.getId() == null) {
                return null;
            }
            return userService.getUserById(principal.getId());
        }
        return null;
    }

    // 1. LIST DATA
    @GetMapping
    public String listProducts(Model model) {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        model.addAttribute("products", productService.getAllProducts(user.getId()));
        return "pages/products/list";
    }

    // 2. FORM TAMBAH
    @GetMapping("/create")
    public String createForm(Model model) {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        model.addAttribute("productForm", new ItemData());
        return "pages/products/form";
    }

    // 3. ACTION SIMPAN (CREATE)
    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute ItemData form, BindingResult result,
            RedirectAttributes redirectAttributes) throws IOException {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        if (result.hasErrors()) {
            return "pages/products/form";
        }

        Item product = new Item();
        product.setUserId(user.getId());
        product.setName(form.getName());
        product.setCategory(form.getCategory());
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setDescription(form.getDescription());

        // Simpan dulu untuk dapat ID
        product = productService.saveProduct(product);

        // Upload Gambar jika ada (pastikan product tidak null dan memiliki ID)
        if (product != null && product.getId() != null) {
            if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
                String filename = fileStorageService.storeFile(form.getImageFile(), product.getId());
                product.setImage(filename);
                productService.saveProduct(product);
            }
        }

        redirectAttributes.addFlashAttribute("success", "Produk berhasil ditambahkan!");
        return "redirect:/products";
    }

    // 4. HALAMAN DETAIL
    @GetMapping("/detail/{id}")
    public String detailProduct(@PathVariable UUID id, Model model) {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        Item product = productService.getProductById(id, user.getId());
        if (product == null)
            return "redirect:/products";

        model.addAttribute("product", product);
        return "pages/products/detail";
    }

    // 5. FORM EDIT
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model) {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        Item product = productService.getProductById(id, user.getId());
        if (product == null)
            return "redirect:/products";

        // Mapping Entity ke DTO (Form)
        ItemData form = new ItemData();
        form.setName(product.getName());
        form.setCategory(product.getCategory());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setDescription(product.getDescription());

        model.addAttribute("productForm", form);
        model.addAttribute("productId", product.getId()); // Penting untuk logika Form HTML
        model.addAttribute("currentImage", product.getImage());
        return "pages/products/form";
    }

    // 6. ACTION UPDATE
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable UUID id, @Valid @ModelAttribute ItemData form, BindingResult result,
            RedirectAttributes redirectAttributes) throws IOException {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        if (result.hasErrors()) {
            // Jika error, kembalikan productId agar form tetap dalam mode Edit
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productForm", result);
            redirectAttributes.addFlashAttribute("productForm", form);
            return "redirect:/products/edit/" + id;
        }

        Item product = productService.getProductById(id, user.getId());
        if (product != null) {
            product.setName(form.getName());
            product.setCategory(form.getCategory());
            product.setPrice(form.getPrice());
            product.setStock(form.getStock());
            product.setDescription(form.getDescription());

            // Cek jika ada upload gambar baru
            if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
                // Hapus gambar lama jika ada
                if (product.getImage() != null) {
                    fileStorageService.deleteFile(product.getImage());
                }
                // Simpan gambar baru
                String filename = fileStorageService.storeFile(form.getImageFile(), product.getId());
                product.setImage(filename);
            }
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil diperbarui!");
        }

        return "redirect:/products";
    }

    // 7. ACTION DELETE (POST)
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";

        Item p = productService.getProductById(id, user.getId());
        if (p != null) {
            // Hapus file fisiknya dulu
            if (p.getImage() != null) {
                fileStorageService.deleteFile(p.getImage());
            }
            // Hapus data di database
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil dihapus!");
        }
        return "redirect:/products";
    }

    // 8. SAFETY NET DELETE (GET)
    // Menangani error jika user refresh halaman delete atau ketik URL manual
    @GetMapping("/delete/{id}")
    public String deleteProductGetHandler() {
        return "redirect:/products";
    }

    // 9. HALAMAN CHART
    @GetMapping("/chart")
    public String chartPage() {
        User user = getAuthenticatedUser();
        if (user == null)
            return "redirect:/auth/login";
        return "pages/products/chart";
    }

    // 10. API DATA CHART
    @GetMapping("/api/chart-data")
    @ResponseBody
    public java.util.List<Object[]> getChartData() {
        User user = getAuthenticatedUser();
        if (user == null)
            return null;
        return productService.getChartData(user.getId());
    }
}