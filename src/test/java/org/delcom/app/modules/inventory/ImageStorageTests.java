package org.delcom.app.modules.inventory;

import org.delcom.app.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.any;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @InjectMocks
    private FileStorageService fileStorageService;

    // JUnit 5 otomatis membuat folder temp yang akan dihapus setelah test selesai
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Set uploadDir ke folder temporary agar test realistik tapi aman
        Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, tempDir.toString());
    }

    // ========================================================
    // 1. TEST STORE FILE (Menutup Baris 20-39)
    // ========================================================

    // Case 1: Folder upload belum ada -> Harus create directory (Baris 22-23)
    @Test
    void storeFile_DirectoryMissing_CreatesDirectoryAndsavesFile() throws IOException, NoSuchFieldException, IllegalAccessException {
        // Arahkan uploadDir ke subfolder yang belum dibuat
        Path subFolder = tempDir.resolve("new_uploads");
        Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, subFolder.toString());

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        UUID id = UUID.randomUUID();
        String storedFilename = fileStorageService.storeFile(mockFile, id);

        // Assert
        assertTrue(Files.exists(subFolder), "Folder harus dibuat otomatis");
        assertTrue(storedFilename.contains("cover_" + id));
        assertTrue(storedFilename.endsWith(".jpg"));
        assertTrue(Files.exists(subFolder.resolve(storedFilename)));
    }

    // Case 2: Filename NULL (Menutup Baris 29: originalFilename != null -> FALSE)
    @Test
    void storeFile_FilenameIsNull_SavesWithoutExtension() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null); // NULL
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        UUID id = UUID.randomUUID();
        String storedFilename = fileStorageService.storeFile(mockFile, id);

        // Assert: Tidak error, tapi ekstensi kosong
        assertFalse(storedFilename.contains(".")); 
    }

    // Case 3: Filename Tidak Punya Titik (Menutup Baris 29: contains(".") -> FALSE)
    @Test
    void storeFile_FilenameHasNoExtension_SavesWithoutExtension() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("filetanpaext"); // Tidak ada titik
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        UUID id = UUID.randomUUID();
        String storedFilename = fileStorageService.storeFile(mockFile, id);

        // Assert
        assertFalse(storedFilename.contains(".")); 
    }

    // ========================================================
    // 2. TEST DELETE FILE (Menutup Baris 42-49)
    // ========================================================

    // Case 1: File Ada -> Berhasil Hapus (Return True)
    @Test
    void deleteFile_FileExists_ReturnsTrue() throws IOException {
        // Buat dummy file dulu
        String filename = "todelete.txt";
        Path filePath = tempDir.resolve(filename);
        Files.createFile(filePath);

        boolean result = fileStorageService.deleteFile(filename);

        assertTrue(result); // Berhasil hapus
        assertFalse(Files.exists(filePath)); // File fisik hilang
    }

    // Case 2: File Tidak Ada -> Gagal Hapus (Return False)
    @Test
    void deleteFile_FileMissing_ReturnsFalse() {
        boolean result = fileStorageService.deleteFile("ghost.txt");
        assertFalse(result); // Gagal karena file gak ada
    }

    // Catatan: Untuk mengetes catch(IOException) di baris 46, kita butuh MockStatic Files.class
    // yang rumit. Biasanya Case 1 & 2 sudah cukup untuk coverage fungsional 90%+.


    // Case 3: Simulasi Error IO (Menutup Catch Block Baris 46-47)
    @Test
    void deleteFile_WhenIOExceptionOccurs_ReturnsFalse() {
        String filename = "corrupt_file.txt";
        
        // Kita menggunakan try-with-resources untuk MockStatic 
        // agar mock ini hanya berlaku di dalam blok ini saja
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            
            // Skenario: Saat Files.deleteIfExists dipanggil dengan Path berapapun,
            // LEMPAR IOException
            filesMock.when(() -> Files.deleteIfExists(any(Path.class)))
                     .thenThrow(new IOException("Simulated Disk Error"));

            // Eksekusi
            boolean result = fileStorageService.deleteFile(filename);

            // Assert: Harus return false karena masuk catch block
            assertFalse(result);
        }
    }
    // ========================================================
    // 3. TEST LOAD FILE (Menutup Baris 51-53)
    // ========================================================
    @Test
    void loadFile_ReturnsCorrectPath() {
        Path result = fileStorageService.loadFile("image.png");
        assertEquals(tempDir.resolve("image.png"), result);
    }

    // ========================================================
    // 4. TEST FILE EXISTS (Menutup Baris 55-57)
    // ========================================================

    // Case 1: File Ada -> True
    @Test
    void fileExists_FilePresent_ReturnsTrue() throws IOException {
        String filename = "exists.txt";
        Files.createFile(tempDir.resolve(filename));

        assertTrue(fileStorageService.fileExists(filename));
    }

    // Case 2: File Tidak Ada -> False
    @Test
    void fileExists_FileMissing_ReturnsFalse() {
        assertFalse(fileStorageService.fileExists("missing.txt"));
    }
}