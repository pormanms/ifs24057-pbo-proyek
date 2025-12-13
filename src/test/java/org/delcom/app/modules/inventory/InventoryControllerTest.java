package org.delcom.app.modules.inventory;

import org.delcom.app.modules.authentication.User;
import org.delcom.app.modules.inventory.ItemService;
import org.delcom.app.modules.inventory.Item;
import org.delcom.app.modules.inventory.InventoryController;
import org.delcom.app.modules.inventory.ItemData;
import org.delcom.app.modules.authentication.AccountService;
import org.delcom.app.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    ItemService productService;
    @Mock
    FileStorageService fileStorageService;
    @Mock
    AccountService userService;
    @Mock
    Model model;
    @Mock
    BindingResult bindingResult;
    @Mock
    RedirectAttributes redirectAttributes;
    @Mock
    SecurityContext securityContext;
    @Mock
    Authentication authentication;

    @InjectMocks
    InventoryController productController;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
    }

    private void mockAuthenticatedUser(boolean isAuthenticated) {
        SecurityContextHolder.setContext(securityContext);
        if (isAuthenticated) {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(mockUser);
            lenient().when(userService.getUserById(mockUser.getId())).thenReturn(mockUser);
        } else {
            lenient().when(securityContext.getAuthentication()).thenReturn(null);
        }
    }

    @Test
    void listProducts_AuthExistsButPrincipalWrongType_RedirectsLogin() {
        // 1. Set Context agar auth != null
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // 2. TAPI, set Principal sebagai String (misal: "anonymousUser"), BUKAN Entity
        // User kita
        // Ini akan membuat kondisi (auth.getPrincipal() instanceof User) menjadi FALSE
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // 3. Panggil method controller
        String view = productController.listProducts(model);

        // 4. Harapannya dianggap user == null, jadi redirect ke login
        assertEquals("redirect:/auth/login", view);
    }

    // 1. LIST DATA
    @Test
    void listProducts_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.listProducts(model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void listProducts_LoggedIn_ReturnsList() {
        mockAuthenticatedUser(true);
        String view = productController.listProducts(model);
        assertEquals("pages/products/list", view);
        verify(productService).getAllProducts(mockUser.getId());
    }

    // 2. FORM TAMBAH
    @Test
    void createForm_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.createForm(model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void createForm_LoggedIn_ReturnsFormPage() {
        mockAuthenticatedUser(true);
        String view = productController.createForm(model);
        assertEquals("pages/products/form", view);
        verify(model).addAttribute(eq("productForm"), any(ItemData.class));
    }

    // 3. ACTION SIMPAN
    @Test
    void saveProduct_NotLoggedIn_RedirectsLogin() throws IOException {
        mockAuthenticatedUser(false);
        String view = productController.saveProduct(new ItemData(), bindingResult, redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void saveProduct_WithValidationErrors_ReturnsForm() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = productController.saveProduct(new ItemData(), bindingResult, redirectAttributes);
        assertEquals("pages/products/form", view);
    }

    @Test
    void saveProduct_Success_Redirects() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        ItemData form = new ItemData();
        Item savedProduct = new Item();
        savedProduct.setId(UUID.randomUUID());
        when(productService.saveProduct(any(Item.class))).thenReturn(savedProduct);

        String view = productController.saveProduct(form, bindingResult, redirectAttributes);
        assertEquals("redirect:/products", view);
    }

    @Test
    void saveProduct_SuccessWithImage_StoresFile() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        ItemData form = new ItemData();
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        form.setImageFile(mockFile);

        Item savedProduct = new Item();
        savedProduct.setId(UUID.randomUUID());
        when(productService.saveProduct(any(Item.class))).thenReturn(savedProduct);

        productController.saveProduct(form, bindingResult, redirectAttributes);
        verify(fileStorageService).storeFile(mockFile, savedProduct.getId());
    }

    @Test
    void saveProduct_FileIsNotNullButEmpty_DoesNotStoreFile() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        ItemData form = new ItemData();

        // MOCK FILE: Tidak Null, TAPI isEmpty() return true
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        form.setImageFile(mockFile); // Kondisi: Not Null && Empty

        Item savedProduct = new Item();
        savedProduct.setId(UUID.randomUUID());

        // Mock save pertama (pembuatan ID)
        when(productService.saveProduct(any(Item.class))).thenReturn(savedProduct);

        String view = productController.saveProduct(form, bindingResult, redirectAttributes);

        // Assert:
        // Pastikan storeFile TIDAK dipanggil karena file kosong
        verify(fileStorageService, never()).storeFile(any(), any());

        // Pastikan saveProduct hanya dipanggil 1x (save awal),
        // tidak ada save kedua (update image)
        verify(productService, times(1)).saveProduct(any(Item.class));

        assertEquals("redirect:/products", view);
    }

    // 4. HALAMAN DETAIL
    @Test
    void detailProduct_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.detailProduct(UUID.randomUUID(), model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void detailProduct_ProductFound_ReturnsPage() {
        mockAuthenticatedUser(true);
        when(productService.getProductById(any(), any())).thenReturn(new Item());
        String view = productController.detailProduct(UUID.randomUUID(), model);
        assertEquals("pages/products/detail", view);
    }

    @Test
    void detailProduct_ProductNotFound_RedirectsToProducts() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();

        // Mock service return NULL
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.detailProduct(pid, model);

        assertEquals("redirect:/products", view);
    }

    // Skenario 2: Produk DITEMUKAN (product != null) -> False
    @Test
    void detailProduct_ProductFound_ReturnsView() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();

        // Mock service return OBJECT
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.detailProduct(pid, model);

        assertEquals("pages/products/detail", view);
        verify(model).addAttribute("product", p);
    }

    // 5. FORM EDIT
    @Test
    void editForm_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.editForm(UUID.randomUUID(), model);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void editForm_ProductNotFound_RedirectsToProducts() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();

        // Mock service return NULL
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.editForm(pid, model);

        assertEquals("redirect:/products", view);
    }

    // Skenario 2: Produk DITEMUKAN (product != null) -> False
    @Test
    void editForm_ProductFound_ReturnsForm() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
        p.setId(pid);
        p.setName("Test Product"); // Set property biar mapping DTO aman
        p.setCategory("General");
        p.setPrice(100.0);
        p.setStock(1);

        // Mock service return OBJECT
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.editForm(pid, model);

        assertEquals("pages/products/form", view);
        // Pastikan model attribute terset
        verify(model).addAttribute(eq("productForm"), any());
    }

    // 6. ACTION UPDATE (BAGIAN YANG BANYAK MERAH)
    @Test
    void updateProduct_NotLoggedIn_RedirectsLogin() throws IOException {
        mockAuthenticatedUser(false);
        String view = productController.updateProduct(UUID.randomUUID(), new ItemData(), bindingResult,
                redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    // FIX BARIS 146-147: Memastikan Flash Attribute dipanggil saat Error
    @Test
    void updateProduct_ValidationErrors_ReturnsEditForm_AndSetsFlashAttributes() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(true); // Trigger error
        UUID pid = UUID.randomUUID();

        String view = productController.updateProduct(pid, new ItemData(), bindingResult, redirectAttributes);

        // Verifikasi baris 146-147 terpanggil
        verify(redirectAttributes).addFlashAttribute(eq("org.springframework.validation.BindingResult.productForm"),
                eq(bindingResult));
        verify(redirectAttributes).addFlashAttribute(eq("productForm"), any(ItemData.class));

        assertEquals("redirect:/products/edit/" + pid, view);
    }

    // FIX BARIS 162-163 & 166: Memastikan deleteFile lama dan storeFile baru
    // terpanggil
    @Test
    void updateProduct_WithNewImageAndExistingOldImage_ReplacesFile() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        existing.setImage("old_image.jpg"); // PENTING: Set image lama agar tidak null

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false); // PENTING: File baru tidak boleh empty
        form.setImageFile(newFile);

        String view = productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        // Verifikasi baris 162-163 (Delete) dan 166 (Store)
        verify(fileStorageService).deleteFile("old_image.jpg");
        verify(fileStorageService).storeFile(newFile, pid);

        assertEquals("redirect:/products", view);
    }

    @Test
    void updateProduct_ProductNotFound_ReturnsRedirect() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        // Mock service return NULL
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.updateProduct(pid, new ItemData(), bindingResult, redirectAttributes);

        // Pastikan langsung redirect tanpa error, dan tidak memanggil save
        assertEquals("redirect:/products", view);
        verify(productService, never()).saveProduct(any());
    }

    @Test
    void updateProduct_FileObjectExistsButEmpty_DoesNotProcessImage() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        // Mock File: Object ada, tapi isEmpty() = true
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        form.setImageFile(mockFile);

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        // Pastikan tidak ada proses store file
        verify(fileStorageService, never()).storeFile(any(), any());
        verify(productService).saveProduct(existing);
    }

    // Jadi logic deleteFile dilewati
    @Test
    void updateProduct_NewImage_ButNoOldImageToDelete_StoresNewOnly() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        existing.setImage(null); // PENTING: Image lama NULL

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false); // File baru valid
        form.setImageFile(newFile);

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        // Verify: deleteFile TIDAK dipanggil (karena null), tapi storeFile DIPANGGIL
        verify(fileStorageService, never()).deleteFile(any());
        verify(fileStorageService).storeFile(newFile, pid);
    }

    @Test
    void updateProduct_FormImageIsNull_DoesNotProcessImage() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        form.setImageFile(null); // KUNCI: Set Explicitly NULL

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        // Verify: Tidak ada interaksi dengan FileStorage
        verify(fileStorageService, never()).storeFile(any(), any());
        verify(fileStorageService, never()).deleteFile(any());

        // Tapi tetap save produk (update nama/harga/dll)
        verify(productService).saveProduct(existing);
    }

    // 7. ACTION DELETE (POST)
    @Test
    void deleteProduct_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.deleteProduct(UUID.randomUUID(), redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    // FIX BARIS 186: Memastikan deleteFile terpanggil saat hapus produk
    @Test
    void deleteProduct_WithImage_DeletesFileAndDb() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
        p.setImage("gambar_exist.jpg"); // PENTING: Set image agar masuk blok if

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.deleteProduct(pid, redirectAttributes);

        // Verifikasi baris 186 terpanggil
        verify(fileStorageService).deleteFile("gambar_exist.jpg");
        verify(productService).deleteProduct(pid);
        assertEquals("redirect:/products", view);
    }

    @Test
    void deleteProduct_ProductNotFound_DoesNothing() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();

        // MOCK: Product tidak ditemukan (NULL)
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.deleteProduct(pid, redirectAttributes);

        // Verify: Tidak ada penghapusan DB maupun File
        verify(productService, never()).deleteProduct(any());
        verify(fileStorageService, never()).deleteFile(any());

        assertEquals("redirect:/products", view);
    }

    @Test
    void deleteProduct_ProductHasNoImage_DeletesDbOnly() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
        p.setId(pid);
        p.setImage(null); // PENTING: Image diset NULL

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.deleteProduct(pid, redirectAttributes);

        // Verify: Hapus DB dipanggil, TAPI Hapus File TIDAK dipanggil
        verify(productService).deleteProduct(pid);
        verify(fileStorageService, never()).deleteFile(any());

        assertEquals("redirect:/products", view);
    }

    // 8. SAFETY NET DELETE (GET)
    @Test
    void deleteProductGetHandler_AlwaysRedirects() {
        String view = productController.deleteProductGetHandler();
        assertEquals("redirect:/products", view);
    }

    // 9. HALAMAN CHART
    @Test
    void chartPage_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.chartPage();
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void chartPage_LoggedIn_ReturnsPage() {
        mockAuthenticatedUser(true);
        String view = productController.chartPage();
        assertEquals("pages/products/chart", view);
    }

    // 10. API DATA CHART
    @Test
    void getChartData_NotLoggedIn_ReturnsNull() {
        mockAuthenticatedUser(false);
        List<Object[]> result = productController.getChartData();
        assertNull(result);
    }

    @Test
    void getChartData_LoggedIn_ReturnsData() {
        mockAuthenticatedUser(true);
        productController.getChartData();
        verify(productService).getChartData(mockUser.getId());
    }

}