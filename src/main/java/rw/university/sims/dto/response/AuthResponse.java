package rw.university.sims.dto.response;

public record AuthResponse(
    String tokenType,
    String accessToken,
    long accessTokenExpiresInSeconds,
    String refreshToken,
    CurrentUserResponse user
) {
}