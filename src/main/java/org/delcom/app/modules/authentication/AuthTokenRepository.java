package org.delcom.app.modules.authentication;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    @Query("SELECT at FROM AuthToken at WHERE at.userId = ?1 AND at.token = ?2")
    AuthToken findUserToken(UUID userId, String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM AuthToken at WHERE at.userId = ?1")
    void deleteByUserId(UUID userId);
}
