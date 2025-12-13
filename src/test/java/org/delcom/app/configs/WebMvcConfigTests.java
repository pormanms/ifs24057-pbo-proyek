package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private ResourceHandlerRegistry resourceHandlerRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private ResourceHandlerRegistration resourceHandlerRegistration;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @BeforeEach
    void setUp() {
        // Mocking chain methods agar tidak NullPointerException saat dipanggil berantai
        lenient().when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        lenient().when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        lenient().when(interceptorRegistration.excludePathPatterns(anyString())).thenReturn(interceptorRegistration);

        lenient().when(resourceHandlerRegistry.addResourceHandler(anyString())).thenReturn(resourceHandlerRegistration);
        lenient().when(resourceHandlerRegistration.addResourceLocations(anyString())).thenReturn(resourceHandlerRegistration);
    }

    @Test
    void addInterceptors_ShouldRegisterAuthInterceptor() {
        webMvcConfig.addInterceptors(interceptorRegistry);

        // Verifikasi bahwa AuthInterceptor didaftarkan
        verify(interceptorRegistry).addInterceptor(authInterceptor);
        
        // Verifikasi konfigurasi path
        verify(interceptorRegistration).addPathPatterns("/api/**");
        verify(interceptorRegistration, atLeastOnce()).excludePathPatterns(anyString());
    }

    @Test
    void addResourceHandlers_ShouldRegisterUploads() {
        webMvcConfig.addResourceHandlers(resourceHandlerRegistry);

        // Verifikasi handler uploads
        verify(resourceHandlerRegistry).addResourceHandler("/uploads/**");
        
        // Karena di kode WebMvcConfig.java path-nya hardcoded "file:./uploads/", 
        // kita verifikasi string tersebut, BUKAN inject variabel uploadDir.
        verify(resourceHandlerRegistration).addResourceLocations("file:./uploads/");
    }
}