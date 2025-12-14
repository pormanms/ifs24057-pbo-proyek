package org.delcom.app.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    // 1. Inisialisasi Logger
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    @Value("${server.port:8080}")
    private int port;

    @Value("${spring.devtools.livereload.enabled:false}")
    private boolean livereload;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,     // 2. Tambahkan @NonNull
            @NonNull HttpServletResponse response,    // 2. Tambahkan @NonNull
            @NonNull FilterChain filterChain          // 2. Tambahkan @NonNull
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        
        // Lanjutkan request ke controller/filter selanjutnya
        filterChain.doFilter(request, response);
        
        long duration = System.currentTimeMillis() - start;

        int status = response.getStatus();
        String color;
        if (status >= 500) {
            color = RED;
        } else if (status >= 400) {
            color = YELLOW;
        } else if (status >= 200) {
            color = GREEN;
        } else {
            color = CYAN;
        }

        // Ambil asal kode dari stacktrace (Opsional: ini cukup berat untuk performa tinggi, tapi oke untuk dev)
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement origin = Arrays.stream(stack)
                .filter(s -> s.getClassName().startsWith("org.delcom"))
                .findFirst()
                .orElse(stack[stack.length - 1]);
        String originInfo = origin.getClassName() + "." + origin.getMethodName() + ":" + origin.getLineNumber();

        String remoteAddr = request.getRemoteAddr();

        // Format pesan log
        // Menggunakan logger {} placeholder lebih efisien daripada String.format jika log level dimatikan
        // Tapi karena kamu butuh string berwarna yang kompleks, String.format tetap oke.
        String logMessage = String.format(
                "%s%-6s %s %d %dms%s [%s] from %s",
                color,
                request.getMethod(),
                request.getRequestURI(),
                status,
                duration,
                RESET,
                originInfo,
                remoteAddr);

        if (!request.getRequestURI().startsWith("/.well-known")) {
            // 3. Gunakan Logger alih-alih System.out
            logger.info("{}", logMessage); 
        }
    }
}