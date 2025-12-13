package org.delcom.app.configs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private ServletWebRequest webRequest;

    @InjectMocks
    private CustomErrorController errorController;

    @Test
    void handleError_Status500_ReturnsErrorLabel() {
        // Percabangan: status == 500 -> "error"
        Map<String, Object> mockAttributes = Map.of(
            "status", 500,
            "error", "Internal Server Error",
            "path", "/api/broken"
        );

        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(mockAttributes);

        ResponseEntity<Map<String, Object>> response = errorController.handleError(webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("error", response.getBody().get("status")); // Validasi logika "error"
    }

    @Test
    void handleError_Status404_ReturnsFailLabel() {
        // Percabangan: status != 500 (contoh: 404) -> "fail"
        Map<String, Object> mockAttributes = Map.of(
            "status", 404,
            "error", "Not Found",
            "path", "/api/missing"
        );

        when(errorAttributes.getErrorAttributes(any(ServletWebRequest.class), any(ErrorAttributeOptions.class)))
                .thenReturn(mockAttributes);

        ResponseEntity<Map<String, Object>> response = errorController.handleError(webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("fail", response.getBody().get("status")); // Validasi logika "fail"
    }
}