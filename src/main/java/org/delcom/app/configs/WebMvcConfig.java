package org.delcom.app.configs;

import org.delcom.app.interceptors.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 1. Ubah menjadi final field
    private final AuthInterceptor authInterceptor;

    // 2. Gunakan Constructor Injection (Solusi SonarLint S6813)
    // Spring otomatis meng-inject dependency lewat constructor ini.
    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) { // 3. Tambahkan @NonNull
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") 
                .excludePathPatterns("/api/auth/**") 
                .excludePathPatterns("/api/public/**"); 
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) { // 4. Tambahkan @NonNull
        // Membuka akses folder uploads ke browser
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}