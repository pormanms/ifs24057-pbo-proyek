package org.delcom.app.configs;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTests {

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private HttpServletRequest request;

    private CustomErrorController customErrorController;

    @BeforeEach
    void setUp() {
        customErrorController = new CustomErrorController(errorAttributes);
    }

    @Test
    void handleError_ReturnsInternalServerError_WhenStatusIs500() {
        // Setup mock attributes
        Map<String, Object> attributes = Map.of(
                "status", 500,
                "error", "Internal Server Error",
                "path", "/test"
        );
        
        // Mock behavior ErrorAttributes
        // Perhatikan: Kita menggunakan any(WebRequest.class) agar fleksibel
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        // Action: Panggil method dengan request biasa (HttpServletRequest)
        // KITA TIDAK LAGI MEMBUNGKUS DENGAN new ServletWebRequest(request) DI SINI
        // KARENA DI CONTROLLER SUDAH DIBUNGKUS SECARA INTERNAL
        ResponseEntity<Map<String, Object>> response = customErrorController.handleError(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().get("status"));
    }

    @Test
    void handleError_ReturnsFail_WhenStatusIs400() {
        Map<String, Object> attributes = Map.of(
                "status", 400,
                "error", "Bad Request",
                "path", "/test"
        );

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(attributes);

        ResponseEntity<Map<String, Object>> response = customErrorController.handleError(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().get("status"));
    }
}