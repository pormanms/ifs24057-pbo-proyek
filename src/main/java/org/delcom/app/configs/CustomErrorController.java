package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    // 1. Definisikan Constant (Solusi SonarLint)
    private static final String ERROR_PATH = "error";

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    // 2. Gunakan Constant di RequestMapping
    @RequestMapping("/" + ERROR_PATH)
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        
        // 3. Bungkus request menjadi ServletWebRequest
        ServletWebRequest webRequest = new ServletWebRequest(request);

        Map<String, Object> attributes = errorAttributes
                .getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());

        int status = (int) attributes.getOrDefault("status", 500);
        String path = (String) attributes.getOrDefault("path", "unknown");

        // Logic status message
        String statusMessage = (status == 500) ? ERROR_PATH : "fail";

        // Menggunakan Map.of untuk response body
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", statusMessage,
                // Mengambil pesan error asli dari attributes
                "error", attributes.getOrDefault(ERROR_PATH, "Unknown Error"), 
                "message", "Endpoint tidak ditemukan atau terjadi error",
                "path", path
        );

        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}