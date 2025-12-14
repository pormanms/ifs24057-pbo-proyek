package org.delcom.app.interceptors;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.modules.authentication.AccountService;
import org.delcom.app.modules.authentication.AuthToken;
import org.delcom.app.modules.authentication.AuthTokenService;
import org.delcom.app.modules.authentication.User;
import org.delcom.app.utils.JwtUtil;
import org.springframework.lang.NonNull; // 1. Import NonNull
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    protected AuthContext authContext;
    protected AuthTokenService authTokenService;
    protected AccountService userService;

    // Constructor Injection tidak memerlukan @Autowired
    public AuthInterceptor(AuthContext authContext, AuthTokenService authTokenService, AccountService userService) {
        this.authContext = authContext;
        this.authTokenService = authTokenService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,   // 2. Tambah @NonNull
            @NonNull HttpServletResponse response,  // 2. Tambah @NonNull
            @NonNull Object handler                 // 2. Tambah @NonNull
    ) throws Exception {
        
        // Skip auth untuk endpoint public
        if (isPublicEndpoint(request)) {
            return true;
        }

        // Ambil bearer token dari header
        String rawAuthToken = request.getHeader("Authorization");
        String token = extractToken(rawAuthToken);

        // Validasi token
        if (token == null || token.isEmpty()) {
            sendErrorResponse(response, 401, "Token autentikasi tidak ditemukan");
            return false;
        }

        // Validasi format token JWT
        if (!JwtUtil.validateToken(token, true)) {
            sendErrorResponse(response, 401, "Token autentikasi tidak valid");
            return false;
        }

        // Ekstrak userId dari token
        UUID userId = JwtUtil.extractUserId(token);
        if (userId == null) {
            sendErrorResponse(response, 401, "Format token autentikasi tidak valid");
            return false;
        }

        // Cari token di database
        AuthToken authToken = authTokenService.findUserToken(userId, token);
        if (authToken == null) {
            sendErrorResponse(response, 401, "Token autentikasi sudah expired");
            return false;
        }

        // Ambil data user
        User authUser = userService.getUserById(authToken.getUserId());
        if (authUser == null) {
            sendErrorResponse(response, 404, "User tidak ditemukan");
            return false;
        }

        // Set user ke auth context
        authContext.setAuthUser(authUser);
        return true;
    }

    private String extractToken(String rawAuthToken) {
        if (rawAuthToken != null && rawAuthToken.startsWith("Bearer ")) {
            return rawAuthToken.substring(7); // hapus "Bearer "
        }
        return null;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 3. Menghapus commented code (dead code) sesuai saran SonarLint

        // Endpoint public yang tidak perlu auth
        return path.startsWith("/api/auth") || path.equals("/error");
    }

    // 4. Ubah Exception menjadi IOException agar lebih spesifik
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"status\":\"fail\",\"message\":\"%s\",\"data\":null}",
                message);
        response.getWriter().write(jsonResponse);
    }
}