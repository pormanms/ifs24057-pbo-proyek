package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoverTodoFormTests {

    private CoverTodoForm coverTodoForm;
    private MultipartFile mockMultipartFile;

    @BeforeEach
    void setup() {
        coverTodoForm = new CoverTodoForm();
        mockMultipartFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Constructor default membuat objek kosong")
    void constructor_default_membuat_objek_kosong() {
        // Act
        CoverTodoForm form = new CoverTodoForm();

        // Assert
        assertNull(form.getId());
        assertNull(form.getCoverFile());
    }

    @Test
    @DisplayName("Setter dan Getter untuk ID bekerja dengan benar")
    void setter_dan_getter_untuk_id_bekerja_dengan_benar() {
        // Arrange
        UUID expectedId = UUID.randomUUID();

        // Act
        coverTodoForm.setId(expectedId);
        UUID actualId = coverTodoForm.getId();

        // Assert
        assertEquals(expectedId, actualId);
    }

    @Test
    @DisplayName("Setter dan Getter untuk coverFile bekerja dengan benar")
    void setter_dan_getter_untuk_coverFile_bekerja_dengan_benar() {
        // Act
        coverTodoForm.setCoverFile(mockMultipartFile);
        MultipartFile actualFile = coverTodoForm.getCoverFile();

        // Assert
        assertEquals(mockMultipartFile, actualFile);
    }

    @Test
    @DisplayName("isEmpty return true ketika coverFile null")
    void isEmpty_return_true_ketika_coverFile_null() {
        // Arrange
        coverTodoForm.setCoverFile(null);

        // Act
        boolean result = coverTodoForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEmpty return true ketika coverFile empty")
    void isEmpty_return_true_ketika_coverFile_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isEmpty();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isEmpty return false ketika coverFile tidak empty")
    void isEmpty_return_false_ketika_coverFile_tidak_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isEmpty();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("getOriginalFilename return null ketika coverFile null")
    void getOriginalFilename_return_null_ketika_coverFile_null() {
        // Arrange
        coverTodoForm.setCoverFile(null);

        // Act
        String result = coverTodoForm.getOriginalFilename();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("getOriginalFilename return filename ketika coverFile ada")
    void getOriginalFilename_return_filename_ketika_coverFile_ada() {
        // Arrange
        String expectedFilename = "test-image.jpg";
        when(mockMultipartFile.getOriginalFilename()).thenReturn(expectedFilename);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        String result = coverTodoForm.getOriginalFilename();

        // Assert
        assertEquals(expectedFilename, result);
    }

    @Test
    @DisplayName("isValidImage return false ketika coverFile null")
    void isValidImage_return_false_ketika_coverFile_null() {
        // Arrange
        coverTodoForm.setCoverFile(null);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return false ketika coverFile empty")
    void isValidImage_return_false_ketika_coverFile_empty() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return false ketika contentType null")
    void isValidImage_return_false_ketika_contentType_null() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn(null);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/jpeg")
    void isValidImage_return_true_untuk_image_jpeg() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/png")
    void isValidImage_return_true_untuk_image_png() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/gif")
    void isValidImage_return_true_untuk_image_gif() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return true untuk image/webp")
    void isValidImage_return_true_untuk_image_webp() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isValidImage();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isValidImage return false untuk content type non-image")
    void isValidImage_return_false_untuk_content_type_non_image() {
        // Arrange
        String[] invalidContentTypes = {
                "text/plain",
                "application/pdf",
                "application/octet-stream",
                "video/mp4",
                "audio/mpeg",
                "image/svg+xml", // SVG tidak didukung
                "image/bmp" // BMP tidak didukung
        };

        for (String contentType : invalidContentTypes) {
            when(mockMultipartFile.isEmpty()).thenReturn(false);
            when(mockMultipartFile.getContentType()).thenReturn(contentType);
            coverTodoForm.setCoverFile(mockMultipartFile);

            // Act
            boolean result = coverTodoForm.isValidImage();

            // Assert
            assertFalse(result, "Should return false for content type: " + contentType);
        }
    }

    @Test
    @DisplayName("isSizeValid return false ketika coverFile null")
    void isSizeValid_return_false_ketika_coverFile_null() {
        // Arrange
        coverTodoForm.setCoverFile(null);
        long maxSize = 1024 * 1024; // 1MB

        // Act
        boolean result = coverTodoForm.isSizeValid(maxSize);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isSizeValid return true ketika file size sama dengan maxSize")
    void isSizeValid_return_true_ketika_file_size_sama_dengan_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        when(mockMultipartFile.getSize()).thenReturn(maxSize);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isSizeValid(maxSize);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isSizeValid return true ketika file size kurang dari maxSize")
    void isSizeValid_return_true_ketika_file_size_kurang_dari_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        long fileSize = 512 * 1024; // 0.5MB
        when(mockMultipartFile.getSize()).thenReturn(fileSize);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isSizeValid(maxSize);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("isSizeValid return false ketika file size lebih dari maxSize")
    void isSizeValid_return_false_ketika_file_size_lebih_dari_maxSize() {
        // Arrange
        long maxSize = 1024 * 1024; // 1MB
        long fileSize = 2 * 1024 * 1024; // 2MB
        when(mockMultipartFile.getSize()).thenReturn(fileSize);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isSizeValid(maxSize);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("isSizeValid return true untuk file size 0 dengan maxSize 0")
    void isSizeValid_return_true_untuk_file_size_0_dengan_maxSize_0() {
        // Arrange
        when(mockMultipartFile.getSize()).thenReturn(0L);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Act
        boolean result = coverTodoForm.isSizeValid(0L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Integration test - form valid untuk image JPEG ukuran normal")
    void integration_test_form_valid_untuk_image_JPEG_ukuran_normal() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        when(mockMultipartFile.getSize()).thenReturn(500 * 1024L); // 500KB
        when(mockMultipartFile.getOriginalFilename()).thenReturn("photo.jpg");

        coverTodoForm.setId(id);
        coverTodoForm.setCoverFile(mockMultipartFile);

        // Assert semua kondisi
        assertFalse(coverTodoForm.isEmpty());
        assertEquals("photo.jpg", coverTodoForm.getOriginalFilename());
        assertTrue(coverTodoForm.isValidImage());
        assertTrue(coverTodoForm.isSizeValid(1024 * 1024)); // 1MB max
        assertEquals(id, coverTodoForm.getId());
    }

    @Test
    @DisplayName("Integration test - form invalid untuk file besar")
    void integration_test_form_invalid_untuk_file_besar() {
        // Arrange
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        when(mockMultipartFile.getSize()).thenReturn(5 * 1024 * 1024L); // 5MB
        when(mockMultipartFile.getOriginalFilename()).thenReturn("large-image.png");

        coverTodoForm.setCoverFile(mockMultipartFile);

        // Assert
        assertFalse(coverTodoForm.isEmpty());
        assertTrue(coverTodoForm.isValidImage()); // Masih valid sebagai image
        assertFalse(coverTodoForm.isSizeValid(2 * 1024 * 1024)); // Tapi size melebihi 2MB
    }

    @Test
    @DisplayName("Edge case - contentType case insensitive")
    void edge_case_contentType_case_insensitive() {
        // Arrange
        String[] caseVariations = {
                "IMAGE/JPEG",
                "Image/Jpeg",
                "image/JPEG",
                "IMAGE/jpeg"
        };

        for (String contentType : caseVariations) {
            when(mockMultipartFile.isEmpty()).thenReturn(false);
            when(mockMultipartFile.getContentType()).thenReturn(contentType);
            coverTodoForm.setCoverFile(mockMultipartFile);

            // Act
            boolean result = coverTodoForm.isValidImage();

            // Assert
            assertFalse(result, "Should return false for case variation: " + contentType);
        }
    }
}