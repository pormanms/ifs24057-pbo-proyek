package org.delcom.app.modules.authentication;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.delcom.app.modules.authentication.SignInRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignInRequestTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid Login Form: Email & Password Benar")
    void testValidLoginForm() {
        SignInRequest form = new SignInRequest();
        form.setEmail("test@example.com");
        form.setPassword("password123");
        form.setRememberMe(true);

        Set<ConstraintViolation<SignInRequest>> violations = validator.validate(form);

        // Harusnya tidak ada error
        assertTrue(violations.isEmpty());
        
        // Cek Getter
        assertEquals("test@example.com", form.getEmail());
        assertEquals("password123", form.getPassword());
        assertTrue(form.isRememberMe());
    }

    @Test
    @DisplayName("Invalid Login Form: Email Format Salah")
    void testInvalidEmailFormat() {
        SignInRequest form = new SignInRequest();
        form.setEmail("bukan-email"); // Format salah
        form.setPassword("password123");

        Set<ConstraintViolation<SignInRequest>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        // Pastikan errornya ada di field email
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Invalid Login Form: Email Kosong")
    void testEmptyEmail() {
        SignInRequest form = new SignInRequest();
        form.setEmail(""); // Kosong
        form.setPassword("password123");

        Set<ConstraintViolation<SignInRequest>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Invalid Login Form: Password Kosong")
    void testEmptyPassword() {
        SignInRequest form = new SignInRequest();
        form.setEmail("test@example.com");
        form.setPassword(""); // Kosong

        Set<ConstraintViolation<SignInRequest>> violations = validator.validate(form);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}