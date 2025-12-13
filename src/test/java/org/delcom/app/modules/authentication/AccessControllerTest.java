package org.delcom.app.modules.authentication;

import org.delcom.app.modules.authentication.AuthController;
import org.delcom.app.modules.authentication.SignInRequest;
import org.delcom.app.modules.authentication.RegisterForm;
import org.delcom.app.modules.authentication.User;
import org.delcom.app.modules.authentication.AccountService;
import org.delcom.app.utils.ConstUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessControllerTest {

    @Mock AccountService userService;
    @Mock HttpSession session;
    @Mock Model model;
    @Mock BindingResult bindingResult;
    @Mock RedirectAttributes redirectAttributes;
    @Mock SecurityContext securityContext;
    // Authentication tidak perlu di-Mock global, kita buat manual di helper

    @InjectMocks AuthController authController;

    @BeforeEach
    void setUp() {
        // Reset SecurityContext sebelum setiap test
        SecurityContextHolder.clearContext();
    }

    // --- HELPER UNTUK MOCK LOGIN STATUS ---
    private void mockLoginStatus(boolean isLoggedIn) {
        SecurityContextHolder.setContext(securityContext);
        if (isLoggedIn) {
            // FIX: Gunakan Constructor 3 argumen (User, Creds, Authorities) 
            // agar property isAuthenticated() otomatis TRUE
            Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", 
                "pass", 
                AuthorityUtils.createAuthorityList("ROLE_USER")
            );
            when(securityContext.getAuthentication()).thenReturn(auth);
        } else {
            // Simulasi User Belum Login (Anonymous)
            Authentication auth = new AnonymousAuthenticationToken(
                "key", 
                "anon", 
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
            );
            when(securityContext.getAuthentication()).thenReturn(auth);
        }
    }

    private void mockNullAuth() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);
    }

    // =========================================================
    // 1. GET LOGIN PAGE
    // =========================================================

    @Test
    void showLogin_WhenAlreadyLoggedIn_RedirectsHome() {
        mockLoginStatus(true); // Logged In -> True
        String view = authController.showLogin(model, session);
        assertEquals("redirect:/", view);
    }

    @Test
    void showLogin_WhenNotLoggedIn_ReturnsLoginPage() {
        mockLoginStatus(false); // Logged In -> False (Anonymous)
        String view = authController.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(model).addAttribute(eq("loginForm"), any(SignInRequest.class));
    }

    @Test
    void showLogin_WhenAuthIsNull_ReturnsLoginPage() {
        mockNullAuth(); // Auth object null
        String view = authController.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

     @Test
    void showLogin_AuthExistsButNotAuthenticated_ReturnsLoginPage() {
        Authentication auth = mock(Authentication.class);
        
        when(auth.isAuthenticated()).thenReturn(false);
        
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);

        String view = authController.showLogin(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
    }

    // =========================================================
    // 2. POST LOGIN
    // =========================================================

    @Test
    void postLogin_ValidationErrors_ReturnsLoginPage() {
        when(bindingResult.hasErrors()).thenReturn(true); // Branch: Error -> True

        String view = authController.postLogin(new SignInRequest(), bindingResult, session, model);
        
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void postLogin_UserNotFound_ReturnsError() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("unknown@mail.com")).thenReturn(null); // Branch: User == null

        SignInRequest form = new SignInRequest();
        form.setEmail("unknown@mail.com");

        String view = authController.postLogin(form, bindingResult, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("belum terdaftar"));
    }

    @Test
    void postLogin_WrongPassword_ReturnsError() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword(new BCryptPasswordEncoder().encode("correctPass")); // Hash DB

        when(userService.getUserByEmail("user@mail.com")).thenReturn(user);

        SignInRequest form = new SignInRequest();
        form.setEmail("user@mail.com");
        form.setPassword("wrongPass"); // Input Salah

        String view = authController.postLogin(form, bindingResult, session, model);

        // Branch: !isPasswordMatch -> True
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_LOGIN, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("salah"));
    }

    @Test
    void postLogin_Success_RedirectsHome() {
        when(bindingResult.hasErrors()).thenReturn(false);
        
        User user = new User();
        user.setEmail("user@mail.com");
        user.setPassword(new BCryptPasswordEncoder().encode("correctPass"));

        when(userService.getUserByEmail("user@mail.com")).thenReturn(user);

        SignInRequest form = new SignInRequest();
        form.setEmail("user@mail.com");
        form.setPassword("correctPass"); // Input Benar

        String view = authController.postLogin(form, bindingResult, session, model);

        assertEquals("redirect:/", view);
        // Pastikan session diset
        verify(session).setAttribute(anyString(), any(SecurityContext.class));
    }

    // =========================================================
    // 3. GET REGISTER PAGE
    // =========================================================

    @Test
    void showRegister_WhenAlreadyLoggedIn_RedirectsHome() {
        mockLoginStatus(true);
        String view = authController.showRegister(model, session);
        assertEquals("redirect:/", view);
    }

    @Test
    void showRegister_WhenNotLoggedIn_ReturnsRegisterPage() {
        mockLoginStatus(false);
        String view = authController.showRegister(model, session);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(model).addAttribute(eq("registerForm"), any(RegisterForm.class));
    }

        @Test
    void showRegister_AuthExistsButNotAuthenticated_ReturnsRegisterPage() {
        // Mock object Authentication biasa
        Authentication auth = mock(Authentication.class);
        
        // Paksa isAuthenticated() jadi FALSE
        when(auth.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);

        String view = authController.showRegister(model, session);

        // Harapan: Dianggap belum login, jadi tetap di halaman register
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

        @Test
    void showRegister_AuthIsAnonymous_ReturnsRegisterPage() {
        // Buat token Anonymous (secara default token ini setAuthenticated(true))
        Authentication auth = new AnonymousAuthenticationToken(
            "key", 
            "anonymousUser", 
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);

        String view = authController.showRegister(model, session);

        // Harapan: Anonymous user dianggap belum login, tetap di register page
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    // auth == NULL
    // Skenario: Security Context benar-benar kosong (belum ada token sama sekali)
    @Test
    void showRegister_WhenAuthIsNull_ReturnsRegisterPage() {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(null);
        String view = authController.showRegister(model, session);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    // =========================================================
    // 4. POST REGISTER
    // =========================================================

    @Test
    void postRegister_ValidationErrors_ReturnsRegisterPage() {
        when(bindingResult.hasErrors()).thenReturn(true);
        String view = authController.postRegister(new RegisterForm(), bindingResult, redirectAttributes, session, model);
        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
    }

    @Test
    void postRegister_EmailAlreadyExists_ReturnsError() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("exist@mail.com")).thenReturn(new User()); // Branch: User != null

        RegisterForm form = new RegisterForm();
        form.setEmail("exist@mail.com");

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("sudah terdaftar"));
    }

    @Test
    void postRegister_CreateUserFailed_ReturnsError() {
        // Skenario: Email aman, tapi saat save DB return null (simulasi error DB)
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        
        // Mock createUser return NULL
        when(userService.createUser(any(), any(), any())).thenReturn(null); // Branch: createdUser == null

        RegisterForm form = new RegisterForm();
        form.setEmail("new@mail.com");
        form.setPassword("pass");

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals(ConstUtil.TEMPLATE_PAGES_AUTH_REGISTER, view);
        verify(bindingResult).rejectValue(eq("email"), anyString(), contains("Gagal membuat"));
    }

    @Test
    void postRegister_Success_RedirectsLogin() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getUserByEmail("new@mail.com")).thenReturn(null);
        
        // Mock createUser SUKSES
        when(userService.createUser(any(), any(), any())).thenReturn(new User());

        RegisterForm form = new RegisterForm();
        form.setEmail("new@mail.com");
        form.setPassword("pass");

        String view = authController.postRegister(form, bindingResult, redirectAttributes, session, model);

        assertEquals("redirect:/auth/login", view);
        verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
    }

    // =========================================================
    // 5. LOGOUT
    // =========================================================

    @Test
    void logout_InvalidatesSessionAndRedirects() {
        String view = authController.logout(session);
        
        verify(session).invalidate();
        assertEquals("redirect:/auth/login", view);
    }
}