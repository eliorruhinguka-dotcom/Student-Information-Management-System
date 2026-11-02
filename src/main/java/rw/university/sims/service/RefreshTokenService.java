package rw.university.sims.service;

import rw.university.sims.entity.RefreshToken;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.RefreshTokenRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.refresh-token-days:7}")
            long refreshTokenDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    public record Rotation(UserAccount user, String rawToken) {
    }

    @Transactional
    public String create(UserAccount user) {
        String rawToken = generateRawToken();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setIssuedAt(now);
        token.setExpiresAt(now.plusDays(refreshTokenDays));

        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Rotation rotate(String rawToken) {
        RefreshToken existing = findLocked(rawToken);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (existing.getRevokedAt() != null) {
            refreshTokenRepository.revokeAllForUser(
                existing.getUser().getId()
            );

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token reuse detected; sessions were revoked"
            );
        }

        if (!existing.getExpiresAt().isAfter(now)) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token has expired"
            );
        }

        UserAccount user = existing.getUser();

        if (!user.isEnabled()) {
            existing.setRevokedAt(now);
            refreshTokenRepository.save(existing);

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Account is disabled"
            );
        }

        existing.setRevokedAt(now);
        refreshTokenRepository.save(existing);

        return new Rotation(user, create(user));
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void revoke(String rawToken, Long expectedUserId) {
        RefreshToken token = findLocked(rawToken);

        if (!token.getUser().getId().equals(expectedUserId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Refresh token does not belong to the authenticated user"
            );
        }

        if (token.getRevokedAt() != null) {
            return;
        }

        token.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void revokeAll(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private RefreshToken findLocked(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token is required"
            );
        }

        return refreshTokenRepository
            .findByHashForUpdate(hash(rawToken))
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid refresh token"
            ));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(
                token.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is unavailable",
                exception
            );
        }
    }
}