package org.delcom.app.modules.authentication;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.delcom.app.modules.authentication.RegisterForm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignUpRequestTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Test Full Getters & Setters (Agar Hijau Semua)")
    void testGettersAndSetters() {
        RegisterForm form = new RegisterForm();

        // 1. Panggil Setter
        form.setName("Budi Santoso");
        form.setEmail("budi@example.com");
        form.setPassword("rahasia123");

        // 2. Panggil Getter (INI YANG PENTING AGAR TIDAK MERAH)
        assertEquals("Budi Santoso", form.getName());       // Line 24 (Sudah Hijau)
        assertEquals("budi@example.com", form.getEmail());  // Line 32 (Akan jadi Hijau)
        assertEquals("rahasia123", form.getPassword());     // Line 40 (Akan jadi Hijau)
    }

    @Test
    @DisplayName("Test Validation: Invalid Email")
    void testInvalidEmail() {
        RegisterForm form = new RegisterForm();
        form.setName("Budi");
        form.setEmail("bukan-email");
        form.setPassword("pass");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);
        
        // Pastikan error terjadi di field email
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Test Validation: Empty Fields")
    void testEmptyFields() {
        RegisterForm form = new RegisterForm();
        // Tidak di-set apa-apa (null)

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);
        
        // Harusnya ada 3 error (Name, Email, Password)
        assertTrue(violations.size() >= 3);
    }
}