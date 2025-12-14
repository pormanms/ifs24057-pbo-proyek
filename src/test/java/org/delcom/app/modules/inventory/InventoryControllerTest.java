package org.delcom.app.modules.inventory;

import org.delcom.app.modules.authentication.User;
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
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        String view = productController.listProducts(model);

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
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        form.setImageFile(mockFile); 

        Item savedProduct = new Item();
        savedProduct.setId(UUID.randomUUID());

        when(productService.saveProduct(any(Item.class))).thenReturn(savedProduct);

        String view = productController.saveProduct(form, bindingResult, redirectAttributes);

        verify(fileStorageService, never()).storeFile(any(), any());
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
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);
        String view = productController.detailProduct(pid, model);
        assertEquals("redirect:/products", view);
    }

    @Test
    void detailProduct_ProductFound_ReturnsView() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
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
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);
        String view = productController.editForm(pid, model);
        assertEquals("redirect:/products", view);
    }

    @Test
    void editForm_ProductFound_ReturnsForm() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
        p.setId(pid);
        p.setName("Test Product");
        p.setCategory("General");
        p.setPrice(100.0);
        p.setStock(1);

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.editForm(pid, model);

        assertEquals("pages/products/form", view);
        verify(model).addAttribute(eq("productForm"), any());
    }

    // 6. ACTION UPDATE
    @Test
    void updateProduct_NotLoggedIn_RedirectsLogin() throws IOException {
        mockAuthenticatedUser(false);
        String view = productController.updateProduct(UUID.randomUUID(), new ItemData(), bindingResult,
                redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void updateProduct_ValidationErrors_ReturnsEditForm_AndSetsFlashAttributes() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(true); 
        UUID pid = UUID.randomUUID();

        String view = productController.updateProduct(pid, new ItemData(), bindingResult, redirectAttributes);

        // Perbaikan Warning SonarLint (Hapus eq() jika argumentnya exact value)
        verify(redirectAttributes).addFlashAttribute("org.springframework.validation.BindingResult.productForm", bindingResult);
        verify(redirectAttributes).addFlashAttribute(eq("productForm"), any(ItemData.class));

        assertEquals("redirect:/products/edit/" + pid, view);
    }

    @Test
    void updateProduct_WithNewImageAndExistingOldImage_ReplacesFile() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        existing.setImage("old_image.jpg"); 

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false); 
        form.setImageFile(newFile);

        String view = productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        verify(fileStorageService).deleteFile("old_image.jpg");
        verify(fileStorageService).storeFile(newFile, pid);

        assertEquals("redirect:/products", view);
    }

    @Test
    void updateProduct_ProductNotFound_ReturnsRedirect() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.updateProduct(pid, new ItemData(), bindingResult, redirectAttributes);

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
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        form.setImageFile(mockFile);

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(productService).saveProduct(existing);
    }

    @Test
    void updateProduct_NewImage_ButNoOldImageToDelete_StoresNewOnly() throws IOException {
        mockAuthenticatedUser(true);
        when(bindingResult.hasErrors()).thenReturn(false);

        UUID pid = UUID.randomUUID();
        Item existing = new Item();
        existing.setId(pid);
        existing.setImage(null);

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(existing);

        ItemData form = new ItemData();
        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);
        form.setImageFile(newFile);

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

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
        form.setImageFile(null);

        productController.updateProduct(pid, form, bindingResult, redirectAttributes);

        verify(fileStorageService, never()).storeFile(any(), any());
        verify(fileStorageService, never()).deleteFile(any());
        verify(productService).saveProduct(existing);
    }

    // 7. ACTION DELETE (POST)
    @Test
    void deleteProduct_NotLoggedIn_RedirectsLogin() {
        mockAuthenticatedUser(false);
        String view = productController.deleteProduct(UUID.randomUUID(), redirectAttributes);
        assertEquals("redirect:/auth/login", view);
    }

    @Test
    void deleteProduct_WithImage_DeletesFileAndDb() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        Item p = new Item();
        p.setImage("gambar_exist.jpg");

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.deleteProduct(pid, redirectAttributes);

        verify(fileStorageService).deleteFile("gambar_exist.jpg");
        verify(productService).deleteProduct(pid);
        assertEquals("redirect:/products", view);
    }

    @Test
    void deleteProduct_ProductNotFound_DoesNothing() {
        mockAuthenticatedUser(true);
        UUID pid = UUID.randomUUID();
        when(productService.getProductById(pid, mockUser.getId())).thenReturn(null);

        String view = productController.deleteProduct(pid, redirectAttributes);

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
        p.setImage(null);

        when(productService.getProductById(pid, mockUser.getId())).thenReturn(p);

        String view = productController.deleteProduct(pid, redirectAttributes);

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