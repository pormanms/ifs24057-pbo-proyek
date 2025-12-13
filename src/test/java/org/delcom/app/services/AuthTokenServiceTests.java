package org.delcom.app.services;

import org.delcom.app.modules.authentication.AuthToken;
import org.delcom.app.modules.authentication.AuthTokenRepository;
import org.delcom.app.modules.authentication.AuthTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTests {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("Find User Token")
    void findUserToken() {
        UUID userId = UUID.randomUUID();
        String token = "abc-123";
        AuthToken mockToken = new AuthToken(userId, token);

        when(authTokenRepository.findUserToken(userId, token)).thenReturn(mockToken);

        AuthToken result = authTokenService.findUserToken(userId, token);

        assertNotNull(result);
        assertEquals(token, result.getToken());
    }

    @Test
    @DisplayName("Create Auth Token")
    void createAuthToken() {
        AuthToken input = new AuthToken(UUID.randomUUID(), "token");
        when(authTokenRepository.save(input)).thenReturn(input);

        AuthToken result = authTokenService.createAuthToken(input);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Delete Auth Token")
    void deleteAuthToken() {
        UUID userId = UUID.randomUUID();
        
        authTokenService.deleteAuthToken(userId);

        verify(authTokenRepository).deleteByUserId(userId);
    }
}