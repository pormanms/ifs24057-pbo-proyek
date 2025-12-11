package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TodoFormTest {

    private TodoForm todoForm;

    @BeforeEach
    void setUp() {
        todoForm = new TodoForm();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(todoForm.getId());
        assertNull(todoForm.getTitle());
        assertNull(todoForm.getDescription());
        assertFalse(todoForm.getIsFinished());
        assertNull(todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("Setter dan Getter untuk id bekerja dengan benar")
    void setterAndGetter_Id_WorksCorrectly() {
        UUID id = UUID.randomUUID();
        todoForm.setId(id);
        assertEquals(id, todoForm.getId());
    }

    @Test
    @DisplayName("Setter dan Getter untuk title bekerja dengan benar")
    void setterAndGetter_Title_WorksCorrectly() {
        String title = "Test Title";
        todoForm.setTitle(title);
        assertEquals(title, todoForm.getTitle());
    }

    @Test
    @DisplayName("Setter dan Getter untuk description bekerja dengan benar")
    void setterAndGetter_Description_WorksCorrectly() {
        String description = "Test Description";
        todoForm.setDescription(description);
        assertEquals(description, todoForm.getDescription());
    }

    @Test
    @DisplayName("Setter dan Getter untuk isFinished bekerja dengan benar - true")
    void setterAndGetter_IsFinished_WorksCorrectly_True() {
        todoForm.setIsFinished(true);
        assertTrue(todoForm.getIsFinished());
    }

    @Test
    @DisplayName("Setter dan Getter untuk isFinished bekerja dengan benar - false")
    void setterAndGetter_IsFinished_WorksCorrectly_False() {
        todoForm.setIsFinished(false);
        assertFalse(todoForm.getIsFinished());
    }

    @Test
    @DisplayName("Setter dan Getter untuk confirmTitle bekerja dengan benar")
    void setterAndGetter_ConfirmTitle_WorksCorrectly() {
        String confirmTitle = "Confirm Title";
        todoForm.setConfirmTitle(confirmTitle);
        assertEquals(confirmTitle, todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("isFinished default value adalah false")
    void isFinished_DefaultValue_IsFalse() {
        assertFalse(todoForm.getIsFinished());
    }

    @Test
    @DisplayName("Semua field dapat diset dan diget dengan nilai berbagai tipe")
    void allFields_CanBeSetAndGet_WithVariousValues() {
        // Arrange
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String title = "My Todo";
        String description = "This is a description";
        boolean isFinished = true;
        String confirmTitle = "CONFIRM";

        // Act
        todoForm.setId(id);
        todoForm.setTitle(title);
        todoForm.setDescription(description);
        todoForm.setIsFinished(isFinished);
        todoForm.setConfirmTitle(confirmTitle);

        // Assert
        assertEquals(id, todoForm.getId());
        assertEquals(title, todoForm.getTitle());
        assertEquals(description, todoForm.getDescription());
        assertEquals(isFinished, todoForm.getIsFinished());
        assertEquals(confirmTitle, todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("Field dapat diset dengan null values")
    void fields_CanBeSet_WithNullValues() {
        // Act
        todoForm.setId(null);
        todoForm.setTitle(null);
        todoForm.setDescription(null);
        todoForm.setConfirmTitle(null);

        // Assert
        assertNull(todoForm.getId());
        assertNull(todoForm.getTitle());
        assertNull(todoForm.getDescription());
        assertNull(todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("Field dapat diset dengan empty strings")
    void fields_CanBeSet_WithEmptyStrings() {
        // Act
        todoForm.setTitle("");
        todoForm.setDescription("");
        todoForm.setConfirmTitle("");

        // Assert
        assertEquals("", todoForm.getTitle());
        assertEquals("", todoForm.getDescription());
        assertEquals("", todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("Field dapat diset dengan blank strings")
    void fields_CanBeSet_WithBlankStrings() {
        // Act
        todoForm.setTitle("   ");
        todoForm.setDescription("   ");
        todoForm.setConfirmTitle("   ");

        // Assert
        assertEquals("   ", todoForm.getTitle());
        assertEquals("   ", todoForm.getDescription());
        assertEquals("   ", todoForm.getConfirmTitle());
    }

    @Test
    @DisplayName("isFinished dapat diubah dari false ke true")
    void isFinished_CanBeChanged_FromFalseToTrue() {
        // Arrange
        todoForm.setIsFinished(false);
        assertFalse(todoForm.getIsFinished());

        // Act
        todoForm.setIsFinished(true);

        // Assert
        assertTrue(todoForm.getIsFinished());
    }

    @Test
    @DisplayName("isFinished dapat diubah dari true ke false")
    void isFinished_CanBeChanged_FromTrueToFalse() {
        // Arrange
        todoForm.setIsFinished(true);
        assertTrue(todoForm.getIsFinished());

        // Act
        todoForm.setIsFinished(false);

        // Assert
        assertFalse(todoForm.getIsFinished());
    }

    @Test
    @DisplayName("Multiple operations pada object yang sama")
    void multipleOperations_OnSameObject() {
        // First set of values
        UUID id1 = UUID.randomUUID();
        todoForm.setId(id1);
        todoForm.setTitle("First Title");
        todoForm.setIsFinished(true);

        assertEquals(id1, todoForm.getId());
        assertEquals("First Title", todoForm.getTitle());
        assertTrue(todoForm.getIsFinished());

        // Second set of values
        UUID id2 = UUID.randomUUID();
        todoForm.setId(id2);
        todoForm.setTitle("Second Title");
        todoForm.setIsFinished(false);

        assertEquals(id2, todoForm.getId());
        assertEquals("Second Title", todoForm.getTitle());
        assertFalse(todoForm.getIsFinished());
    }
}