package org.delcom.app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Todo;
import org.delcom.app.entities.User;
import org.delcom.app.services.TodoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

public class TodoControllerTests {
    @Test
    @DisplayName("Pengujian untuk controller Todo")
    void testTodoController() throws Exception {
        // Buat random UUID
        UUID userId = UUID.randomUUID();
        UUID todoId = UUID.randomUUID();
        UUID nonexistentTodoId = UUID.randomUUID();

        // Membuat dummy data
        Todo todo = new Todo(userId, "Belajar Spring Boot", "Belajar mock repository di unit test", false);
        todo.setId(todoId);

        // Membuat mock ServiceRepository
        // Buat mock
        TodoService todoService = Mockito.mock(TodoService.class);

        // Atur perilaku mock
        when(todoService.createTodo(any(UUID.class), any(String.class), any(String.class))).thenReturn(todo);

        // Membuat instance controller
        TodoController todoController = new TodoController(todoService);
        assert (todoController != null);

        todoController.authContext = new AuthContext();
        User authUser = new User("Test User", "testuser@example.com");
        authUser.setId(userId);

        // Menguji method createTodo
        {
            // Data tidak valid
            {
                List<Todo> invalidTodos = List.of(
                        // Title Null
                        new Todo(userId, null, "Deskripsi valid", false),
                        // Title Kosong
                        new Todo(userId, "", "Deskripsi valid", false),
                        // Description Null
                        new Todo(userId, "Judul valid", null, false),
                        // Description Kosong
                        new Todo(userId, "Judul valid", "", false));

                ResponseEntity<ApiResponse<Map<String, UUID>>> result;
                for (Todo itemTodo : invalidTodos) {
                    result = todoController.createTodo(itemTodo);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Tidak terautentikasi untuk menambahkan todo
            {
                todoController.authContext.setAuthUser(null);

                var result = todoController.createTodo(todo);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Berhasil menambahkan todo
            {
                todoController.authContext.setAuthUser(authUser);
                var result = todoController.createTodo(todo);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method getAllTodos
        {
            // Tidak terautentikasi untuk getAllTodos
            {
                todoController.authContext.setAuthUser(null);

                var result = todoController.getAllTodos(null);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Menguji getAllTodos dengan search null
            {
                todoController.authContext.setAuthUser(authUser);

                List<Todo> dummyResponse = List.of(todo);
                when(todoService.getAllTodos(any(UUID.class), any(String.class))).thenReturn(dummyResponse);
                var result = todoController.getAllTodos(null);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method getTodoById
        {
            // Tidak terautentikasi untuk getTodoById
            {
                todoController.authContext.setAuthUser(null);

                var result = todoController.getTodoById(todoId);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            todoController.authContext.setAuthUser(authUser);

            // Menguji getTodoById dengan ID yang ada
            {
                when(todoService.getTodoById(any(UUID.class), any(UUID.class))).thenReturn(todo);
                var result = todoController.getTodoById(todoId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
                assert (result.getBody().getData().get("todo").getId().equals(todoId));
            }

            // Menguji getTodoById dengan ID yang tidak ada
            {
                when(todoService.getTodoById(any(UUID.class), any(UUID.class))).thenReturn(null);
                var result = todoController.getTodoById(nonexistentTodoId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

        }

        // Menguji method updateTodo
        {
            // Data tidak valid
            {
                List<Todo> invalidTodos = List.of(
                        // Title Null
                        new Todo(userId, null, "Deskripsi valid", false),
                        // Title Kosong
                        new Todo(userId, "", "Deskripsi valid", false),
                        // Description Null
                        new Todo(userId, "Judul valid", null, false),
                        // Description Kosong
                        new Todo(userId, "Judul valid", "", false),
                        // isFinished Null
                        new Todo(userId, "Judul valid", "Deskripsi valid", null));

                for (Todo itemTodo : invalidTodos) {
                    var result = todoController.updateTodo(todoId, itemTodo);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Tidak terautentikasi untuk updateTodo
            {
                todoController.authContext.setAuthUser(null);

                var result = todoController.updateTodo(todoId, todo);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            todoController.authContext.setAuthUser(authUser);

            // Memperbarui todo dengan ID tidak ada
            {
                when(todoService.updateTodo(any(UUID.class), any(UUID.class), any(String.class), any(String.class),
                        any(Boolean.class)))
                        .thenReturn(null);
                Todo updatedTodo = new Todo(userId, "Belajar Spring Boot - Updated", "Deskripsi updated", true);
                updatedTodo.setId(nonexistentTodoId);

                var result = todoController.updateTodo(nonexistentTodoId, updatedTodo);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Memperbarui todo dengan ID ada
            {
                Todo updatedTodo = new Todo(userId, "Belajar Spring Boot - Updated", "Deskripsi updated", true);
                updatedTodo.setId(todoId);
                when(todoService.updateTodo(any(UUID.class), any(UUID.class), any(String.class), any(String.class),
                        any(Boolean.class)))
                        .thenReturn(updatedTodo);

                var result = todoController.updateTodo(todoId, updatedTodo);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // // Menguji method deleteTodo
        {
            // Tidak terautentikasi untuk deleteTodo
            {
                todoController.authContext.setAuthUser(null);

                var result = todoController.deleteTodo(todoId);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            todoController.authContext.setAuthUser(authUser);

            // Menguji deleteTodo dengan ID yang tidak ada
            {
                when(todoService.deleteTodo(any(UUID.class), any(UUID.class))).thenReturn(false);
                var result = todoController.deleteTodo(nonexistentTodoId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Menguji deleteTodo dengan ID yang ada
            {
                when(todoService.deleteTodo(any(UUID.class), any(UUID.class))).thenReturn(true);
                var result = todoController.deleteTodo(todoId);
                assert (result != null);
                assert (result.getBody().getStatus().equals("success"));
            }
        }
    }
}
