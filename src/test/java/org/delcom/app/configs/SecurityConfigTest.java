package org.delcom.app.configs;

import org.delcom.app.modules.authentication.AccountService; // IMPORT PENTING
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // IMPORT PENTING
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- TAMBAHAN PENTING (Agar tidak error ApplicationContext) ---
    // Kita memalsukan AccountService agar test tidak mencoba connect ke DB asli
    @MockBean
    private AccountService accountService;
    // -------------------------------------------------------------

    @Test
    void permitAll_forAuthUrls() throws Exception {
        // Pastikan AccessController punya @RequestMapping("/auth") dan @GetMapping("/login")
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void permitAll_forApiUrls() throws Exception {
        // Test URL sembarang API, harusnya 4xx (Forbidden/Unauthorized) atau 404
        // Tergantung SecurityConfig, tapi biasanya authenticated() jadi 401/403
        mockMvc.perform(get("/api/test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void redirect_toLogin_ifNotAuthenticated() throws Exception {
        // Mencoba akses dashboard tanpa login
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login")); // Pastikan sesuai config kamu
    }

    @Test
    void accessDenied_redirectsToLogout() throws Exception {
        // Mencoba akses halaman admin dengan role USER biasa
        mockMvc.perform(get("/admin")
                .with(user("testuser").roles("USER"))) 
                .andExpect(status().is4xxClientError()); // Expect 403 Forbidden
    }

    @Test
    void passwordEncoder_shouldBeBCrypt() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder.encode("test")).isNotBlank();
    }
}